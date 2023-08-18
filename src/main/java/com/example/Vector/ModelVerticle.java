package com.example.Vector;

import com.example.config.MySQLConfig;
import com.example.lib.Get;
import com.example.lib.MyAbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;


public class ModelVerticle extends MyAbstractVerticle {
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



}
