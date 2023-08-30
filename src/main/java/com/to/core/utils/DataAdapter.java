package com.to.core.utils;

import com.to.core.base.DataAdapterEncoder;
import com.to.core.base.Vector;
import io.netty.util.concurrent.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

import java.util.Arrays;
import java.util.Map;

public class DataAdapter {
  private Vector vector = null;
  public DataAdapter(Vector vector) {
    this.vector = vector;
  }
  //生成映射模板
  //存储到redis中
  public String genMapping(String document, JsonObject protocol){
    String result = null;
    //协议编码为映射模板
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(protocol.toString());
    JsonObject mappingObj = new JsonObject();
    mapping.forEach((k, v)->{
      System.out.println(k + " , " + v);
      mappingObj.put(k, v);
    });
    Promise<String> promise = Promise.promise();
    this.vector.getRedis().connect().onSuccess(conn->{
      RedisAPI.api(conn).set(Arrays.asList(document, mappingObj.toString()))
        .onSuccess(res->{
          conn.close();
          promise.complete("ok");
        }).onFailure(err->{
          conn.close();
          promise.complete("failure");
        });
    });
    return promise.future().result();
  }
}
