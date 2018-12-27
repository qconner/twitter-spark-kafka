FROM openjdk:8

RUN mkdir -p /app
WORKDIR /app

COPY docker-service/start-service.sh /app
COPY target/scala-2.11/twitter-spark-kafka.jar /app

EXPOSE 4040

CMD ["sh", "/app/start-service.sh"]
