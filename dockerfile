FROM vertx/vertx4

ENV VERTICLE_NAME com.waa.casting.MainVerticle
ENV VERTICLE_FILE casting-1.0.0-SNAPSHOT.jar

ENV VERTICLE_HOME .

EXPOSE 8888

COPY $VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]