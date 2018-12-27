#!/bin/sh
# uncomment for debugging
eval exec java ${JAVA_OPTS} -jar /app/twitter-spark-kafka.jar

# uncomment for normal production, after successful configuration
#eval exec java ${JAVA_OPTS} -jar /app/twitter-spark-kafka.jar 2> /dev/null
