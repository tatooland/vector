package com.example.lib;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;

public class RedisHelper {
  private String redisUrl = "redis://localhost:6379";
  private RedisConnection client = null;
  private RedisAPI redis = null;
  public RedisHelper(Vertx vertx){
    Promise<RedisConnection> rc = Promise.promise();
     Redis.createClient(vertx, redisUrl)
      .connect()
      .onSuccess(conn -> {
        rc.complete();
      });
  }
  public String get(String key, Handler handler){
    Promise<String> ret = Promise.promise();
    if(this.client == null){
      return "None";
    }else {
      redis = RedisAPI.api(this.client);
      redis.get(key).onSuccess(handler);
      return ret.future().result();
    }
  }
  public void Close() {
    if(this.client != null) {
      this.client.close();
      this.client = null;
    }
  }
}
