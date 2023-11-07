package com.to.core.base.ast;

public class ASTConditionNode extends ASTElement{
  private String express=null;
  public ASTConditionNode(){
    this.setRole("condition");
  }
  public void setExpress(String express){
    this.express = express;
  }
  public String getExpress(){
    return this.express;
  }
}
