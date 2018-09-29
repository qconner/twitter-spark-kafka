package net.qns.delphinus.twitter.gateway

import org.scalatest._

class HelloSpec
    extends FlatSpec
    with Matchers {
  "The Boot object" should "self identify" in {
    Boot.greeting shouldEqual "Twitter Kafka Gateway"
  }
}
