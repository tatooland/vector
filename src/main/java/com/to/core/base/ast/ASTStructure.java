package com.to.core.base.ast;

import java.util.ArrayList;

public class ASTStructure {
  private ArrayList<String> structure=null;
  private String currentStage = null;
  public ASTStructure(){
    this.structure = new ArrayList<>();
  }
  public void setStructure(int flag){
    switch(flag){
      case 3:{
        if(this.currentStage==null){
          this.currentStage= "_start";
        }else if(this.getLastStruct(1).equals("condition_def")){
          this.currentStage = "field_start";
        }else if(this.getLastStruct(1).equals("field_def")){
          this.currentStage = "table_def";
        }
        this.structure.add(this.currentStage);
      }break;
      case 4:{
        if(this.getLastStruct(1).equals("field_def")){
          this.currentStage = "field_def";
        }else if(this.getLastStruct(1).equals("field_def_")){
          this.currentStage = "end";
        }
        this.structure.add(this.currentStage);
      }break;
      case 1:{
        if(this.getLastStruct(1).equals("_start")){
          this.currentStage = "table_def";
        }else if(this.getLastStruct(1).equals("field_def")){
//          this.resetStruct("table_def");
          this.currentStage = "table_def";
        }else if(this.getLastStruct(1).equals("field_contributing")){
          this.currentStage = "condition_contribute";
        }
        this.structure.add(this.currentStage);
      }break;
      case 2:{
        if(this.getLastStruct(1).equals("table_def")){
          this.currentStage = "condition_def";
        }
        this.structure.add(this.currentStage);
      }break;
      case 6: {
        if(this.getLastStruct(1).equals("field_start")){
          this.currentStage = "field_def";
        }else if(this.getLastStruct(1).equals("table_def")){
          this.currentStage = "field_def";
        }
        this.structure.add(this.currentStage);
      }break;
    }
  }
  public String getLastStruct(){
      if(this.structure.size() < 2){
        return null;
      }else {
        return this.structure.get(this.structure.size() - 2);
      }
  }
  public String getLastStruct(int countDownIndex) {return this.structure.get(structure.size()-countDownIndex);}
  public void resetStruct(String stage){
    this.structure.remove(this.structure.size()-1);
    this.structure.add(stage);
  }
  public void PrintStructure(){
    for (String s : this.structure) {
      System.out.println(s);
    }
  }
  public String getCurrentStage(){
    return this.currentStage;
  }
  public String decodeFlag(int id){
    switch(id){
      case 1: {return "(";}
      case 2: {return ")";}
      case 3: return "{";
      case 4: return "}";
      case 5: return ":";
      case 6: return ",";
      default: return "null";
    }
  }
}
