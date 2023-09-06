package com.to.core.utils;

import com.to.core.base.DataAdapterDecoder;
import com.to.core.base.DataAdapterEncoder;
import com.to.core.base.Vector;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.Arrays;
import java.util.Map;

public class DataAdapter {
  private Vector vector = null;
  public DataAdapter(Vector vector) {
    this.vector = vector;
  }
  //生成映射模板
  //存储到redis中
  public Future genMapping(String document, JsonObject protocol){
    //协议编码为映射模板
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(protocol.toString());
    JsonObject mappingObj = new JsonObject();
    mapping.forEach((k, v)->{
      mappingObj.put(k, v);
    });

    return this.vector.getRedis().connect().compose(conn->{
      Future future = RedisAPI.api(conn).set(Arrays.asList(document, mappingObj.toString()));
      conn.close();
      return future;
    });
  }
  public Future queryMapping(String document){
    return this.vector.getRedis().connect().compose(conn->{
      Future future = RedisAPI.api(conn).get(document);
      conn.close();
      return future;
    });
  }

  public String fetchData(RowSet<Row> rs, Map<String, String> tplMapEntry){
    DataAdapterDecoder dataAdapterDecoder = new DataAdapterDecoder();
    return (String) dataAdapterDecoder.setMapping(tplMapEntry).decode(rs);
  }

}
