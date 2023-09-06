package com.to.core.base;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataAdapterEncoder {
  //当前映射为单例还是多例类型

  //将协议配置转换为映射模板
  public Map<String, String> encode(String protocolStr){
    Map<String, String> tplMapEntry = new LinkedHashMap<>();
    //协议分析
    this.analysis(protocolStr, tplMapEntry);
    return tplMapEntry;
  }
  //执行转换
  public Object convert(){
    Object ret=null;
    return ret;
  }
  /*辅助方法*/
  //分析类型：单例，多例
  private void analysis(String protocolStr, Map<String, String> tplMapEntry){
    //判断协议模板中匹配多例模式或是单例模式"{[","]}"
    if(protocolStr.indexOf("[{") != -1 && protocolStr.indexOf("}]")!=-1){
      //表示当前为多例模式
      tplMapEntry.put("vector_framework_data_type", "multiple");
      //多例模式下需要报错起始修饰串和终止修饰串
      int beginIndex = protocolStr.indexOf("[{");
      int endIndex = protocolStr.indexOf("]");
      tplMapEntry.put("vector_framework_multiple_start", protocolStr.substring(0,beginIndex+1));
      tplMapEntry.put("vector_framework_multiple_end", protocolStr.substring(endIndex, protocolStr.length()));
      tplMapEntry.put("vector_framework_multiple_unit_end", "\"}");
      //将起始修饰串和终止修饰串从协议模板中剔除
      protocolStr = protocolStr.substring(beginIndex+1, endIndex);
    }else{
      tplMapEntry.put("vector_framework_data_type", "single");
    }
    this.analysisProtocol(protocolStr,tplMapEntry,0);
  }
  //解析数据交互协议

  private void analysisProtocol(String jsonStr, Map<String, String> tplMapEntry, int start){
    if (jsonStr.indexOf("${", start)!=-1) {
      String preFix = jsonStr.substring(start, jsonStr.indexOf("${", start));
      start = jsonStr.indexOf("${", start);
      int end = jsonStr.indexOf("}$", start);
      String tplKey = jsonStr.substring(start+2, end);
      tplMapEntry.put(tplKey, preFix);
      this.analysisProtocol(jsonStr, tplMapEntry, end+2);
    }else{
      if(tplMapEntry.get("vector_framework_data_type").equals("single")) {
        tplMapEntry.put("vector_framework_single_end", jsonStr.substring(start));
      }
    }
  }
}
