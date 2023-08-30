package com.to.core.utils;

import com.to.core.base.DataAdapterEncoder;
import com.to.core.base.Vector;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;

import java.util.Map;

public class DataAdapter {
  private Vector vector = null;
  private Redis rds = null;
  public DataAdapter(Vector vector) {
    vector = vector;
    rds = vector.getRedis();
  }
  //生成映射模板
  //存储到redis中
  public String genMapping(String document, JsonObject protocol){
    String result = null;
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(protocol.toString());
    JsonObject mappingObj = JsonObject.mapFrom(mapping);
    return mappingObj.toString();
  }
}
