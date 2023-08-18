package com.example.Vector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MainVerticle extends AbstractVerticle {
  MySQLPool pool;
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();
    vertx.deployVerticle(()->new AbstractVerticle(){
      @Override
      public void start() {
        pool = MySQLPool.pool("", new PoolOptions()
          .setMaxSize(5)
          .setShared(true)
          .setName("pool"));
      }
    }, new DeploymentOptions().setInstances(4));
  }
}
