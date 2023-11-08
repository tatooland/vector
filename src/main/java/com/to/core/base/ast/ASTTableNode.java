package com.to.core.base.ast;

public class ASTTableNode extends ASTElement{
  private ASTConditionNode conditionNode = null;
  private ASTFieldNode fieldNode = null;//链表
  private boolean gate = true;//用于标识是否进行过遍历,只有在process状态下才可以设置未真
  public ASTTableNode(){
    this.setRole("table");
  }
  public void mountCondition(ASTConditionNode conditionNode){
    this.conditionNode = conditionNode;
  }
  public void mountField(ASTFieldNode fieldNode){
    this.fieldNode = fieldNode;
  }

  public ASTFieldNode getField(){
    return this.fieldNode;
  }
  public ASTConditionNode getCondition(){
    return this.conditionNode;
  }
  public boolean gateState(){
    return this.gate;
  }
  public void close(){
    this.gate = false;
  }
}
