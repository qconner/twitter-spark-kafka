package net.qns.delphinus.twitter.gateway

// Java
import java.io.File

// Scala
//import scala.util.{Try, Success, Failure}
import collection.JavaConversions._
import scala.util.matching.Regex

// typesafe / lightbend config
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

// Spark
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

// Spark Streaming
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

// Twitter
import org.apache.spark.streaming.twitter.TwitterUtils



trait Greeting {
  lazy val greeting: String = "Twitter Kafka Gateway"
}
object StaticConfig {
  // default config in src/main/resources.application.conf
  private val baseConfig = ConfigFactory.load()

  // local config settings in environment.conf
  val config = ConfigFactory.parseFile(new File("environment.conf")).withFallback(baseConfig)
}

object Boot
    extends App
    with StrictLogging
    with Greeting {

  override def main(args: Array[String]) = {

    println(greeting)


    // twitter API 
    val config = StaticConfig.config
    val twitterConf = config.getConfig("twitter")
    configureTwitter(twitterConf)

    // keyword filters
    val filters = config.getConfig("filter").getStringList("keywords").toList

    logger.info("Connecting to Twitter streaming API")

    // RRD batch period in secs
    val sparkConf = config.getConfig("spark")
    val ssPeriod = sparkConf.getInt("period-in-secs")
    val (ssc, stream) = twitterStream(twitterConf, sparkConf, filters, ssPeriod)

    // extract text from all tweets
    val tweets: DStream[String] = stream filter (_.getLang == "en") map { x =>
      //println(x.getCreatedAt + "  " + x.getText take 168)
      //println(x.getId)
      //println(x.getContributors)
      //println(x.getCreatedAt)
      //println(x.getUser)
      //println(x.getPlace)
      //println(x.getGeoLocation)
      x.getText.replaceAll("\n", "  ")  // replace newlines with spaces
    }

    // partition into two more streams
    val reTweets = stream filter (_.isRetweet == true) map (_.getText.replaceAll("\n", "  "))  // replace newlines with spaces
    val originalTweets = stream filter (_.isRetweet == false) map (_.getText.replaceAll("\n", "  "))  // replace newlines with spaces

    // tweet occurrences
    val tweetCount: DStream[Int] = tweets.map(x => 1).reduceByWindow( (x, y) => x + y, Seconds(ssPeriod), Seconds(ssPeriod))
    val originalTweetCount: DStream[Int] = originalTweets.map(x => 1).reduceByWindow( (x, y) => x + y, Seconds(ssPeriod), Seconds(ssPeriod))
    val reTweetCount: DStream[Int] = reTweets.map(x => 1).reduceByWindow( (x, y) => x + y, Seconds(ssPeriod), Seconds(ssPeriod))

    // forward original tweets to kafka
    originalTweets foreachRDD { rdd =>
      rdd.collect.map( t => {
        // fire off future to send to kafka
        if (t.size > 0) {
          Producer.sendTweet(t)
        }
      })
    }

    def topTenFormat(xs: List[(String, Int)]): List[String] = {
      xs map ( t => {
        val count = t._2
        val rt = t._1
        val pattern = new Regex("""^RT\s@(\w+):\s(.+)$""")
        val (who, what) = rt match {
          case pattern(match1, match2) =>
            ( match1, match2 )
          case _ =>
            throw new Exception("could not parse: " + rt)
        }
        val url = s"http://twitter.com/${who}"
        "%3d: %-20s  %s".format(count, url, what)
      })
    }

    // forward re-tweets to a different kafka topic
    reTweets foreachRDD { rdd =>
      val xs = rdd.collect
      xs.map( t => {
        // fire off future to send to kafka
        if (t.size > 0) {
          Producer.sendReTweet(t)
        }
      })
      val tt = xs.groupBy(identity).mapValues(_.size).toList.sortBy(_._2)(Ordering[Int].reverse).take(32)
      //filter(_._2 > 1)
      Producer.sendTopReTweets("")
      topTenFormat(tt).foreach {
        Producer.sendTopReTweets(_)
      }
    }


    // print item counts
    tweetCount foreachRDD { rdd: RDD[Int] =>
      rdd.collect.size match {
        case x if (x > 0) =>
          val n = rdd.collect.head
          print("%3d tweets".format(n) + " (%.1f/sec)".format(n.toDouble / ssPeriod) + " (%.1f/min)".format(n.toDouble / ssPeriod * 60.0))
        case _ =>
      }
    }
    originalTweetCount foreachRDD { rdd: RDD[Int] =>
      rdd.collect.size match {
        case x if (x > 0) =>
          val n = rdd.collect.head
          print("  %3d original tweets".format(n))
        case _ =>
      }
    }
    reTweetCount foreachRDD { rdd: RDD[Int] =>
      rdd.collect.size match {
        case x if (x > 0) =>
          val n = rdd.collect.head
          println("  %3d re-tweets".format(n))
        case _ =>
      }
    }

    // start the spark job
    ssc.start
    
    // run until killed
    ssc.awaitTermination
  }


  //  set System props for Twitter4J API
  def configureTwitter(conf: Config): Unit = {
    System.setProperty("twitter4j.oauth.consumerKey", conf.getString("consumer-api-key"))
    System.setProperty("twitter4j.oauth.consumerSecret", conf.getString("consumer-secret"))
    System.setProperty("twitter4j.oauth.accessToken", conf.getString("access-token"))
    System.setProperty("twitter4j.oauth.accessTokenSecret", conf.getString("access-token-secret"))
  }

  // return a Spark Streaming Context and Spark DStream of tweets
  def twitterStream(twitterConf: Config, sparkConf: Config, filters: List[String], period: Int): (StreamingContext, InputDStream[twitter4j.Status]) = {
    logger.debug("creating Spark context")

    val sc = new SparkConf()
      .setAppName(greeting)
      .set("spark.streaming.receiverRestartDelay", twitterConf.getString("stream-restart-delay-in-msec"))
      .set("spark.master", sparkConf.getString("master"))
    logger.info(sc.toDebugString)

    val ssc = new StreamingContext(sc, Seconds(period))

    val stream = TwitterUtils.createStream(ssc, None, filters)
    (ssc, stream)
  }
}
