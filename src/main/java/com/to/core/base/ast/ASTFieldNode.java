package com.to.core.base.ast;

public class ASTFieldNode extends ASTElement{
  private ASTTableNode pTable = null;
  public ASTFieldNode(){
    this.setRole("field");
  }
  public void mountPTable(ASTTableNode tableNode){
    this.pTable = tableNode;
  }
  public ASTTableNode getPtable(){
    return this.pTable;
  }
}
