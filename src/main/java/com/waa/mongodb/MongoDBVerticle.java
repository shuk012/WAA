package com.waa.mongodb;

import com.mongodb.MongoClientURI;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import io.netty.channel.EventLoopGroup;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class MongoDBVerticle extends AbstractVerticle {
  public static final Logger logger = LoggerFactory.getLogger(MongoDBVerticle.class);
  public static final String DB = "casting";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    String template = "mongodb://%s:%s@%s/?replicaSet=rs0&readpreference=%s";
    String username = "ec2user";
    String password = "Nish12abcd!";
    String clusterEndpoint = "docdb-2021-01-01-23-19-17.cluster-cae4ria95ugh.us-east-1.docdb.amazonaws.com:27017";
    String readPreference = "secondaryPreferred";
    String connectionString = String.format(template, username, password, clusterEndpoint, readPreference);
    final MongoClient mongoClient = MongoClients.create(connectionString);
    final ClientSession mongoSession = null;
    eventBus.consumer("mongodb.getCollections", message -> processGetCollections(mongoClient, mongoSession, message));
    eventBus.consumer("mongodb.postCollection", message -> processInsertCollection(mongoClient, mongoSession, message));
    startPromise.tryComplete();
  }

  public void processGetCollections(MongoClient mongoClient, ClientSession mongoSession, Message<Object> message) {
    String collectionName = message.body().toString();
    logger.info("collection being listed is {}", collectionName);
    JsonArray response = get(collectionName, mongoClient, mongoSession);
    if (!response.isEmpty()) {
      message.reply(response.encode());
    }
  }

  public void processInsertCollection(MongoClient mongoClient, ClientSession mongoSession, Message<Object> message) {
    JsonObject jsonObject = (JsonObject) message.body();
    logger.info("collection being listed is {}", jsonObject);
    insertCollection(jsonObject, mongoClient, mongoSession);
    message.reply("200");
  }

  public static JsonArray get(String collectionName, MongoClient mongoClient, ClientSession mongoSession) {
    JsonArray response = new JsonArray();
    try {
      MongoCursor cursor = mongoClient.getDatabase(DB).getCollection(collectionName).find().cursor();
      while (cursor.hasNext()) {
        Document document = (Document) cursor.next();
        response.add(document.toJson());
      }
      logger.info(response.encodePrettily());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return response;
  }

  public static void insertCollection(JsonObject jsonObject, MongoClient mongoClient, ClientSession mongoSession) {
    try {
      mongoClient.getDatabase(DB).getCollection(jsonObject.getString("collectionName")).insertOne(Document.parse(jsonObject.toString()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
