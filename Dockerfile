FROM openjdk:8

RUN mkdir -p /app
WORKDIR /app

COPY docker-service/start-service.sh /app
COPY target/scala-2.11/twitter-spark-kafka.jar /app

EXPOSE 4040

#ENV SPARK_MASTER mesos://mesos:5050
#ENV ROOT_LOG_LEVEL debug
#ENV LOG_LEVEL debug
#ENV KAFKA_BOOTSTRAP_SERVERS localhost:9092

ENV TWIT_CONSUMER_API_KEY wwwwwwwwwwwwwwwwwwwwwwwww
ENV TWIT_CONSUMER_SECRET xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
ENV TWIT_ACCESS_TOKEN yyyyyyyyyyyyyyyyyy-yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
ENV TWIT_ACCESS_TOKEN_SECRET zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz

CMD ["sh", "/app/start-service.sh"]
