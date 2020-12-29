package com.waa.workers;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDBVerticle {
  public static final Logger logger = LoggerFactory.getLogger(MDBVerticle.class);
  private static final String COLLECTION_NAME = "breakdown";

  public static void attach(Router router, EventBus eventBus) {
//    router.post("/insertBreakdown").handler(routingContext -> insertBreakdown(routingContext,eventBus));
    router.get("/listBreakdowns").handler(routingContext -> listBreakdowns(routingContext,eventBus));
  }

  private static void listBreakdowns(RoutingContext routingContext, EventBus eventBus) {
    logger.info("Listing all breakdowns");
    eventBus.request("mongodb.get", COLLECTION_NAME, ar -> {
      if (ar.succeeded()) {
        logger.info("Received result for listing breakdowns {}", ar.result().body().toString());
        routingContext.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(200)
          .end(ar.result().body().toString());
      } else {
        logger.error(ar.cause().getLocalizedMessage());
        routingContext.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(500)
          .end(ar.cause().getLocalizedMessage());
      }
    });
  }

}
