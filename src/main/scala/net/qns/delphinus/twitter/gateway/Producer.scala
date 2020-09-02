package net.qns.delphinus.twitter.gateway

// Scala
import scala.collection.JavaConversions._
import scala.util.Random

// Scala Logging
import com.typesafe.scalalogging.StrictLogging

// typesafe config
import com.typesafe.config.{Config, ConfigFactory}

// Kafka
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

// NewRelic
import com.newrelic.api.agent.NewRelic

object KeyGenerator {
  val random = new Random

  def randomKey(N: Int): String = {
    val legalCharacters = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9'))
    Stream.continually(random.nextInt(legalCharacters.size)).map(legalCharacters.mkString).take(N).mkString
  }

  def get(): String = randomKey(8)
}


object Producer
    extends StrictLogging {

  lazy private val kafkaConfig = StaticConfig.config.getConfig("kafka")
  lazy private val tweetTopicText = kafkaConfig.getString("tweet-topic-text")
  lazy private val reTweetTopicText = kafkaConfig.getString("retweet-topic-text")
  lazy private val reTweetTopTenTopicText = kafkaConfig.getString("retweet-top-ten-topic-text")

  // get the singleton KafkaProducer
  lazy val producer: KafkaProducer[String, String] = {

    // scala Map for Kafka configuration
    lazy val kafkaParameters = Map(
      "bootstrap.servers" -> kafkaConfig.getString("bootstrap.servers"),
      "key.serializer" -> kafkaConfig.getString("key.serializer"),
      "value.serializer" -> kafkaConfig.getString("value.serializer")
    )

    // new KafkaProducer
    val p = new KafkaProducer[String, String](kafkaParameters)
    logger.info("Producer singleton created")
    logger.info("writing to Kafka topic: " + tweetTopicText)
    logger.info("writing to Kafka topic: " + reTweetTopicText)
    p
  }

  // tweet
  def sendTweet(x: String) = {
    send("-- " + x, Producer.tweetTopicText)
    NewRelic.incrementCounter(s"Custom/KafkaProducer/${Producer.tweetTopicText}")
  }

  // re-tweet
  def sendReTweet(x: String) = {
    send(x, Producer.reTweetTopicText)
    NewRelic.incrementCounter(s"Custom/KafkaProducer/${Producer.reTweetTopicText}")
  }

  // re-tweet top ten
  def sendTopReTweets(x: String) = {
    send(x, Producer.reTweetTopTenTopicText)
    NewRelic.incrementCounter(s"Custom/KafkaProducer/${Producer.reTweetTopTenTopicText}")
  }


  // send text to Kafka
  private def send(x: String, topic: String): Unit = {
    val rec = new ProducerRecord[String, String](topic, KeyGenerator.get, x)
    send(rec)
  }

  // send ProducerRecord to Kafka
  private def send(record: ProducerRecord[String, String]): Unit = {
    // run future then wait synchronously
    // TODO: make Producer an actor and send AKKA message
    logger debug "prepare:   " + record.toString
    val future = producer.send(record)
    logger debug "queued:    " + future.toString
    /*
    future.onComplete {
      val result = future.get
      logger debug "result:    " + result.toString
    }
     */
  }


}
