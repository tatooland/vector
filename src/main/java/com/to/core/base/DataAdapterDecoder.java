package com.to.core.base;

import java.util.Map;

public class DataAdapterDecoder {
  private String type = null;
  private String multipleStart = null;
  private String multipleEnd = null;
  private String singleEnd = null;
  private Map<String, String> tplMapEntry = null;
  public DataAdapterDecoder setMapping(Map<String, String> tplMapEntry){
    type = tplMapEntry.get("vector_framework_data_type");
    switch(type){
      case "multiple":{
        this.multipleStart = tplMapEntry.get("vector_framework_multiple_start");
        this.multipleEnd = tplMapEntry.get("vector_framework_multiple_end");
      }break;
      case "single": {
        this.singleEnd = tplMapEntry.get("vector_framework_single_end");
      } break;
    }
    this.tplMapEntry = tplMapEntry;
    return this;
  }
  //将映射模板转换为数据实体
  public Object decode(String ...fields){
    String result = "";
    for (String field : fields) {
      result += tplMapEntry.get(field) + field;
    }
    if(type.equals("single")){result += singleEnd;}
    if(type.equals("multiple")){result = multipleStart + result + multipleEnd;}
    return result;
  }
}
