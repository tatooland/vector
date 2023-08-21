package com.example.Vector;

import com.example.config.MySQLConfig;
import com.to.core.ann.Get;
import com.to.core.ann.VectorConfig;
import com.to.core.base.Vector;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;


public class ModelVerticle extends Vector {
  @VectorConfig(independentMySqlPool = true, independentRedisPool = true, port=7777)
  public void init(){}
  @Get(path="/test1")
  public void ModelAdd(RoutingContext ctx){
    //模型入库
    //数据库操作
    Promise<String> promise = Promise.promise();
    Redis client = Redis.createClient(vertx, "redis://localhost:6379");

    client.connect().onSuccess(conn ->{
      RedisAPI.api(conn)
        .get("author")
        .onSuccess(val->{
          this.retResponse(ctx, val.toString());
        });
    });



  }
  @Get(path="/test2")
  public void ModelUpdate(RoutingContext ctx){
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(MySQLConfig.port)
      .setHost(MySQLConfig.url)
      .setDatabase("")
      .setUser("root")
      .setPassword(MySQLConfig.pwd);

    ctx.response()
      .putHeader("content-type", "text/plain")
      .end("model update");
  }
  @Get(path="/test3")
  public void ModelDel(RoutingContext ctx){
    ctx.response()
      .putHeader("content-type", "text/plain")
      .end("model delete");
  }
  @Get(path="/test4")
  public void retResponse(RoutingContext ctx, String val){
    if(val == null){
      System.out.println("none");
    }else {
      ctx.response().putHeader("content-type", "text/plain").end(val);
    }
  }
//
//  @Post(path="/test5")
//  public void sqlCompose(RoutingContext ctx){
//    JsonObject reqObj = ctx.getBody().toJsonObject();
//    pool.getConnection().compose(conn -> {
//      return conn.preparedQuery("select count(1) from users where username=?")
//        .execute(Tuple.of(reqObj.getString("username")))
//        .compose(rows -> {
//          boolean flag = false;
//          for (Row row : rows) {
//            if(row.getInteger("count(1)") > 0) {
//              flag = true;
//              break;
//            }
//          }
//          if(!flag) {
//            return conn.preparedQuery("insert into users(username, password) values(?, ?)")
//              .execute(Tuple.of(reqObj.getString("username"), reqObj.getString("password")))
//              .onComplete(res -> {
//                conn.close();
//                if(res.succeeded()){
//                  ctx.json(new JsonObject().put("result", "success"));
//                }else{
//                  ctx.json(new JsonObject().put("result", "failure"));
//                }
//              });
//          }else{
//            conn.close();
//            ctx.json(new JsonObject().put("result", "repeat"));
//            return Future.succeededFuture();
//          }
//        });
//    });
//  }


}
