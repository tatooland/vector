package com.to.core.base.ast;

public class ASTTableNode extends ASTElement{
  private ASTConditionNode conditionNode = null;
  private ASTFieldNode fieldNode = null;//链表
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
}
