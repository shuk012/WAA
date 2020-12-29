package com.waa.casting;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.ClientSession;
import com.waa.workers.MDBVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setInstances(getMaxProcessors()), stringAsyncResult -> {
      if (stringAsyncResult.succeeded()) {
        logger.info("Deployed {}", MainVerticle.class.getSimpleName());
      } else {
        logger.error("Failed Deployment - {}", stringAsyncResult.cause().getLocalizedMessage());
      }
    });
  }

  private static int getMaxProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    final Router router = Router.router(vertx);
    router.route()
      .handler(BodyHandler.create())
      .failureHandler(routingContext -> {
        if (routingContext.response().ended()) {
          // Ignore completed response
          return;
        }
        logger.error("Route Error:", routingContext.failure());
        routingContext.response()
          .setStatusCode(500)
          .end(new JsonObject().put("message", "Something went wrong :(").toBuffer());
      });
    MDBVerticle.attach(router);
    vertx.createHttpServer().requestHandler(req -> req.response()
      .putHeader("content-type", "text/plain")
      .end("Hello from Vert.x!")).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
