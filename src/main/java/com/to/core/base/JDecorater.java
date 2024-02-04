package com.to.core.base;

import java.util.ArrayList;

public class JDecorater {
  ArrayList<String> fieldLink = new ArrayList<>();
  ArrayList<String> args = new ArrayList<>();
  public JDecorater() {
    this.fieldLink.add("root");
  }
  public String of(ArrayList<String> lines){
    String result = "";
    int depth = 0;
    for(String line:lines){
      line = line.replace(" ", "").trim();
      String[] codeState = line.split(",");
      String type = codeState[0];
      String token = codeState[1];
      String action = codeState[2];

      if(type.equals("table")){
        if(action.equals("process")){
          this.fieldLink.add(token);
          result += this.declareObj(token);
          System.out.println("table_process: " + result);
        }else if(action.equals("return")){
          this.fieldLink.removeLast();
        }
      }else if(type.equals("field")){
        if(action.equals("process")){
          depth += 1;
          this.args.add(token);
          result += this.declareField(token);
          System.out.println("field_process: " + result);
        }else if(action.equals("return")){
          depth -= 1;
        }
        if(depth == 0){
          //返回父元素

        }
      }
    }
    return result;
  }
  public String declareObj(String token){
    return "JsonObject "+token+"=new JsonObject();\n";
  }
  public String declareField(String token){
    String parent = this.fieldLink.getLast();
    return parent + ".put(\"" + token + "\", _" + token + ");\n";
  }

}
