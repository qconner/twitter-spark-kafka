import sbt._

trait Libraries {
  // unit test framework
  lazy val scalaTest          = "org.scalatest"                  %% "scalatest"                   % "3.0.5"

  // log4j logging
  lazy val logback            = "ch.qos.logback"                  % "logback-classic"             % "1.2.3"
  lazy val scalaLogging       = "com.typesafe.scala-logging"     %% "scala-logging"               % "3.9.0"

  // typesafe config
  lazy val typesafeConfig     = "com.typesafe"                    % "config"                      % "1.3.3"

  // 2.3.1 spark is out
  lazy val sparkCore          = "org.apache.spark"               %% "spark-core"                  % "2.2.2"//     % "provided"
  lazy val sparkSQL           = "org.apache.spark"               %% "spark-sql"                   % "2.2.2"//     % "provided"
  lazy val sparkStreaming     = "org.apache.spark"               %% "spark-streaming"             % "2.2.2"//     % "provided"
  //lazy val sparkMesos         = "org.apache.spark"               %% "spark-mesos"                 % "2.2.2"

  // twitter stream (Consumer)
  lazy val twitter            = "org.apache.bahir"               %% "spark-streaming-twitter"     % "2.2.1"

  // 2.0 kafka is out (Producer)
  lazy val kafka              = "org.apache.kafka"               %  "kafka-clients"               % "1.1.1"
  //lazy val sparkKafka         = "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"

  // JSON
  lazy val circeGeneric       = "io.circe"                       %% "circe-generic"                   % "0.9.3"
  lazy val circeParser        = "io.circe"                       %% "circe-parser"                   % "0.9.3"
  //lazy val akkaHttpCirce      = "de.heikoseeberger"              %% "akka-http-circe"                 % "1.21.0"

  // NewRelic
  // https://mvnrepository.com/artifact/com.newrelic.agent.java/newrelic-api
  lazy val newrelic           = "com.newrelic.agent.java"        %  "newrelic-api"                % "5.14.0"

}

object Dependencies extends Libraries {
    lazy val allSpecified = Seq(
      scalaTest,
      logback,
      scalaLogging,
      typesafeConfig,
      sparkCore,
      sparkSQL,
      sparkStreaming,
      twitter,
      kafka,
      circeGeneric,
      circeParser,
      newrelic
  )
}
