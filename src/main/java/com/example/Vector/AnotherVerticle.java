package com.example.Vector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

public class AnotherVerticle extends AbstractVerticle {
  protected MySQLPool pool = null;
  @Override
  public void start() {
    System.out.println("redis verticle");
    this.initSQL();
    Router router = Router.router(vertx);
//    router.route().method(HttpMethod.POST).method(HttpMethod.PUT);
    //获取body参数必须要加入下面这段
    router.route().handler(BodyHandler.create());
    router.route("/user").handler(this::handleUser);
    router.route("/foo").handler(this::handleFoo);//route()同时支持get post访问
    router.post("/foo1").handler(this::handlePost);
    router.get("/get").handler(this::handleGet);
    router.post("/consumes").consumes("object/mytag").handler(this::handleConsumes);//请求只有设置了Content-Type: text/plain(后改成了object/mytag)后才能被处理器响应
    router.post("/sql").handler(this::handleSQL);
    router.post("/sqlCallback").handler(this::handleCallbackSQL);
    router.post("/sqlCompose").handler(this::handleComposeSQL);
    vertx.createHttpServer().requestHandler(router).listen(7778);
  }

  private void handleConsumes(RoutingContext ctx) {
    ctx.json(new JsonObject().put("content", "consumes"));
  }
  private void handleGet(RoutingContext ctx) {
    ctx.json(new JsonObject().put("method", "HTTP_GET"));
  }
  private void handlePost(RoutingContext ctx) {
    ctx.json(new JsonObject().put("key", "value"));
  }
  public void handleUser(RoutingContext routingContext){
    JsonObject object = new JsonObject();
    object.put("foo", "bar").put("num", 123).put("mybool", true);
    routingContext.response()
      .putHeader("content-type", "text/plain")
      .end(object.toString());
  }

  private void handleFoo(RoutingContext ctx) {
    ctx.json(new JsonObject().put("hello", "vert.x"));
//    ctx.json(new JsonArray().add("vertx").add("web"));
  }


  private void handleSQL(RoutingContext ctx) {

//      System.out.println(ctx.getBody().toString());
      JsonObject reqObj = ctx.getBody().toJsonObject();
      pool.preparedQuery("insert into users(username, password) values(?, ?)")
            .execute(
              Tuple.of(reqObj.getString("username"), reqObj.getString("password")))
            .onSuccess( rows -> {
              for (Row row : rows) {
                System.out.println("row = " + row.toString());
              }
            });
      ctx.response().end("success");
  }

  private void handleComposeSQL(RoutingContext ctx) {
    JsonObject reqObj = ctx.getBody().toJsonObject();
    pool.getConnection().compose(conn -> {
      return conn.preparedQuery("select count(1) from users where username=?")
        .execute(Tuple.of(reqObj.getString("username")))
        .compose(rows -> {
          boolean flag = false;
          for (Row row : rows) {
            if(row.getInteger("count(1)") > 0) {
              flag = true;
              break;
            }
          }
          if(!flag) {
            return conn.preparedQuery("insert into users(username, password) values(?, ?)")
              .execute(Tuple.of(reqObj.getString("username"), reqObj.getString("password")))
              .onComplete(res -> {
                conn.close();
                if(res.succeeded()){
                  ctx.json(new JsonObject().put("result", "success"));
                }else{
                  ctx.json(new JsonObject().put("result", "failure"));
                }
              });
          }else{
            conn.close();
            ctx.json(new JsonObject().put("result", "repeat"));
            return Future.succeededFuture();
          }
        });
    });
  }
  private void handleCallbackSQL(RoutingContext ctx) {
    JsonObject reqObj = ctx.getBody().toJsonObject();
    pool.getConnection().onSuccess( conn -> {
      conn.preparedQuery("select count(1) from users where username=?")
        .execute(Tuple.of(reqObj.getString("username")))
        .onComplete(row -> {
          if(row.succeeded()) {
            boolean flag = false;
            for (Row row1 : row.result()) {
              if (row1.getInteger("count(1)") > 0) {
                flag = true;
                break;
              }
            }
            if (!flag) {
              conn.preparedQuery("insert into users(username, password) values(?, ?)")
                .execute(Tuple.of(reqObj.getString("username"), reqObj.getString("password")))
                .onComplete(re -> {
                  conn.close();
                  if (re.succeeded()) {
                    ctx.json(new JsonObject().put("result", "success"));
                  } else {
                    ctx.json(new JsonObject().put("result", "failure"));
                  }
                });
            }else{
              conn.close();
              ctx.json(new JsonObject().put("result", "repeat"));
            }
          }else{
            conn.close();
            ctx.json(new JsonObject().put("result", "error"));
          }
        });
    });
  }

  private void initSQL() {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("127.0.0.1")
      .setDatabase("zoo")
      .setUser("root")
      .setPassword("()<>JK2019T^^km");

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    pool = MySQLPool.pool(vertx, connectOptions, poolOptions);
    pool.getConnection().onSuccess(conn -> {
      System.out.println("Got a connection from the pool");
    });
  }
}
