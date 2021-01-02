package com.waa.casting;

import com.waa.mongodb.MongoDBVerticle;
import com.waa.workers.MDBVerticle;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    Future<String> mainVerticleFuture = vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setInstances(getMaxProcessors()));
    mainVerticleFuture.onComplete(ar -> {
      if (ar.succeeded()) {
        logger.info("All verticles have been deployed");
      } else {
        logger.error(ar.cause().getLocalizedMessage());
      }
    });
  }

  private static int getMaxProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(MongoDBVerticle.class.getName(), new DeploymentOptions().setWorker(true).setWorkerPoolName("MongoDBWorker").setWorkerPoolSize(5))
      .onFailure(startPromise::fail)
      .onSuccess(id -> logger.info("Deployed {} with id {}", MongoDBVerticle.class.getSimpleName(), id));
    final Router router = Router.router(vertx);
    final EventBus eventBus = vertx.eventBus();
    router.route()
      .handler(BodyHandler.create())
      .failureHandler(routingContext -> {
        if (routingContext.response().ended()) {
          return;
        }
        logger.error("Route Error: {}", routingContext.failure().getLocalizedMessage());
        routingContext.response()
          .setStatusCode(500)
          .end(new JsonObject().put("message", "Something went wrong :(").toBuffer());
      });
    MDBVerticle.attach(router,eventBus);
    vertx.createHttpServer()
      .requestHandler(router)
      .exceptionHandler(error -> logger.error("HTTP Server error: ", error)).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.tryComplete();
        logger.info("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
