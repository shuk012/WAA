package com.waa.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class MongoDB {
  public static final Logger logger = LoggerFactory.getLogger(MongoDB.class);
  public static final String COLLECTION = "breakdown";
  public static final String DB = "casting";
  static MongoClient mongoClient = MongoClients.create();
  static ClientSession mongoSession = mongoClient.startSession();

  public static void insert(JsonObject breakdowns) {
    try {
      mongoSession.startTransaction();
      mongoClient.getDatabase(DB)
        .getCollection(COLLECTION)
        .insertOne(mongoSession, Document.parse(breakdowns.toString()));
      mongoSession.commitTransaction();
      logger.info("Inserted {} Successfully", breakdowns);
    } catch (Exception e) {
      logger.error("Insert Failed - {}", e.getLocalizedMessage());
      mongoSession.abortTransaction();
    }
  }

  public static JsonArray get() {
    final JsonArray response = new JsonArray();
    for (Document document : mongoClient.getDatabase(DB)
      .getCollection(COLLECTION)
      .find()) {
      response.add(document.toJson());
    }
    return response;
  }
}
