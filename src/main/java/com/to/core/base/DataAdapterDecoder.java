package com.to.core.base;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DataAdapterDecoder {
  private String type = null;
  private String multipleStart = null;
  private String multipleEnd = null;
  private String multipleUnitEnd = null;
  private String singleEnd = null;
  private Map<String, String> tplMapEntry = null;
  public DataAdapterDecoder setMapping(Map<String, String> tplMapEntry){
    type = tplMapEntry.get("vector_framework_data_type");
    switch(type){
      case "multiple":{
        this.multipleStart = tplMapEntry.get("vector_framework_multiple_start");
        this.multipleEnd = tplMapEntry.get("vector_framework_multiple_end");
        this.multipleUnitEnd = tplMapEntry.get("vector_framework_multiple_unit_end");
      }break;
      case "single": {
        this.singleEnd = tplMapEntry.get("vector_framework_single_end");
      } break;
    }
    this.tplMapEntry = tplMapEntry;
    return this;
  }
  //将映射模板转换为数据实体
  public Object decode(RowSet<Row> rs){
    long startTime=System.currentTimeMillis();
    String result = "";
    Set<String> keys = tplMapEntry.keySet();
    if(type.equals("single")){
      for(Row row : rs){
        for (String key : keys) {
          result += tplMapEntry.get(key) + row.getString(key) +",";
        }
        result = result.substring(result.length()-1);
      }
      result += singleEnd;
    }
    if(type.equals("multiple")){
      for(Row row : rs){
        for (String key : keys) {
          if(key.equals("vector_framework_data_type") ||
            key.equals("vector_framework_multiple_start") ||
            key.equals("vector_framework_multiple_end") ||
            key.equals("vector_framework_multiple_unit_end")
          ){}else{
            result += tplMapEntry.get(key) + row.getString(key);
          }
        }
        result = result.substring(0,result.length()-1) + multipleUnitEnd+ ",";
      }
      result = result.substring(0,result.length()-1);
      result = multipleStart + result + multipleEnd;
    }
    long endTime = System.currentTimeMillis();
    System.out.println("解码运行时间：" + (endTime-startTime) + "ms");
    return result;
  }

  public Object simpleDecode(RowSet<Row> rs, JsonObject jsObject){
    String result = "'result':[";
    ArrayList<String> keys = new ArrayList<>();
    jsObject.getMap().forEach((k, v)->{
      keys.add(k);
    });
    for (Row r : rs) {
      result += "{";
      for (String key : keys) {
            result += r.getString(key) + ",";
      }
      result = result.substring(0, result.length()-1)+"},";
    }
    result = result.substring(0, result.length()-1) + "]";
    return result;
  }
  public Object simpleDecode(RowSet<Row> rs, ArrayList<String> keys){
    String result = "'result':[";
    for (Row r : rs) {
      result += "{";
      for (String key : keys) {
        result += r.getString(key) + ",";
      }
      result = result.substring(0, result.length()-1)+"},";
    }
    result = result.substring(0, result.length()-1) + "]";
    return result;
  }
}
