package com.waa.workers;

import com.waa.mongodb.MongoDB;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDBVerticle {
  public static final Logger logger = LoggerFactory.getLogger(MDBVerticle.class);
  public static void attach(Router router) {
    router.post("/insertBreakdown").handler(MDBVerticle::insertBreakdown);
    router.get("/listBreakdowns").handler(MDBVerticle::listBreakdowns);
  }

  private static void listBreakdowns(RoutingContext routingContext) {
    logger.info("Listing all breakdowns");
    JsonArray breakdownsList = MongoDB.get();
    logger.info("Path {} responds with {}", routingContext.normalizedPath(), breakdownsList.encode());
    routingContext.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .putHeader("my-header", "my-value")
      .end(breakdownsList.toBuffer());
  }

  private static void insertBreakdown(RoutingContext context) {
    JsonObject JsonBody = context.getBodyAsJson();
    logger.info("The received request body is - {}",JsonBody);
    MongoDB.insert(JsonBody);
    final JsonArray response = new JsonArray();
    logger.info("Path {} responds with {}", context.normalizedPath(), response.encode());
    context.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .putHeader("my-header", "my-value")
      .end(response.toBuffer());
  }
}
