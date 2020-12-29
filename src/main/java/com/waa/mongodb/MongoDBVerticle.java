package com.waa.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
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
    var eventBus = vertx.eventBus();
    try (MongoClient mongoClient = MongoClients.create()) {
      final ClientSession mongoSession = mongoClient.startSession();
      eventBus.consumer("mongodb.get", message -> {
        String collectionName = message.body().toString();
        JsonArray response = get(collectionName, mongoClient, mongoSession);
        if (!response.isEmpty()) {
          message.reply(response.encode());
        }
      });
    }catch (Exception e){
      logger.error(e.getMessage());
      startPromise.fail(e.getMessage());
    }
    startPromise.complete();
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
}
