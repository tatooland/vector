package com.to.core.base.ast;

public class ASTElement {
  private String token = null;
  private String identify = null;
  private String alias = null;//别名
  private String type = null;//root,tableName,condition,field
  private String modifiers = null;//修饰词，condition,field类型下才可以使用
  private ASTElement child = null;//下一级节点
  private ASTElement parent = null;//父节点
  private ASTElement younger = null;//弟弟节点
  private ASTElement elder = null;//哥哥节点
  public void setIdentify(String identify){
    this.identify = identify;
    //判断是否存在别名
    if(identify.indexOf(":")!=-1) {
      String[] id = identify.split(":");
      this.setToken(id[0]);
      this.setAlias(id[1]);
    }else{
      this.setToken(identify);
    }
  }
  public void setToken(String token){
    this.token = token;
  }
  public void setAlias(String alias){
    this.alias = alias;
  }
  public void mountSon(ASTElement son){
    this.child = son;
    son.mountParent(this);
  }
  public void mountYounger(ASTElement younger){
    this.younger = younger;
    younger.mountElder(this);
  }
  public void mountParent(ASTElement parent){
    this.parent = parent;
  }
  public void mountElder(ASTElement elder){
    this.elder = elder;
  }
  public void setRole(String role){
    this.type = role;
  }
  public ASTElement getChild(){
    return this.child;
  }
  public ASTElement getParent(){
    return this.parent;
  }
  public ASTElement getYounger(){
    return this.younger;
  }
  public ASTElement getElder(){
    return this.elder;
  }
  public String getToken(){
    return this.token;
  }
  public String getAlias(){
    return this.alias;
  }
  public String getModifiers(){
    return this.modifiers;
  }
  public String getRole(){
    return this.type;
  }
}
