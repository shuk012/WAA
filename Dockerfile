FROM amazoncorretto:latest

ENV VERTICLE_NAME com.waa.casting.MainVerticle
ENV VERTICLE_FILE ./target/casting-1.0.0-SNAPSHOT-fat.jar

ENV VERTICLE_HOME .

EXPOSE 8888

COPY $VERTICLE_FILE $VERTICLE_HOME/fat.jar

WORKDIR $VERTICLE_HOME
ENTRYPOINT java -jar fat.jar
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]
