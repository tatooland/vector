package com.to.core.base;

import java.util.ArrayList;

public class JSONAdapter {
  public JSONAdapter() {

  }
  public String parse(String jsonString){
    ArrayList<String> statements = new ArrayList<>();
    char[] charCode = jsonString.replace(" ", "").trim().toCharArray();
    int actionFlag = -1;//扫描区域标识位
    /*
    0--START: 起始点
    1--KEY_READY: 新建key
    2--KEY_COMPLETE key构建完毕
    3--
    * */

    for (char c : charCode) {
      switch(c){
        case '{':{
          //准备新建一个key
          actionFlag = 1;
        }break;
      }
    }
    String code = null;
    for (String statement : statements) {
      code += statement + "\n";
    }
    return code;
  }
  public static String formatJSON(String jsonStr){
    String result = jsonStr.replace(" ", "").trim();
    int pos = 0;
    int stop = 0;
    String left = null;
    String right = null;
    while(stop != -1) {
      pos = result.indexOf(":\"", pos);
      int temp_p1 = result.indexOf("\",", pos);
      int temp_p2 = result.indexOf("\"}", pos);
      if( temp_p1 < temp_p2 && temp_p1 != -1) {
        stop = result.indexOf("\",", pos);
      }else{
        stop = result.indexOf("\"}", pos+1);
      }
      left = result.substring(0, pos);
      right = result.substring(stop+1);
      result = left + right;
      if(temp_p1 == -1){
        break;
      }
    }
//    result = result.replace(":{", "()");

    return result.replace("\"", "").replace(":{", "(){");
  }
}
