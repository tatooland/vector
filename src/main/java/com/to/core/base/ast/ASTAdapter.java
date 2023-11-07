package com.to.core.base.ast;


import java.util.ArrayList;

public class ASTAdapter {
  private ASTRootNode root = null;
  private ASTElement currentNode = null;
  private boolean state_0=true;//起始标志
  protected char scanChar='1';//当前扫描的字符
  protected ArrayList<Character> wordSlot=null;//当前扫描的单词
  protected ArrayList<String> words = new ArrayList<>();

  protected int pos1 = 0;//标识1
  protected int pos2 = 0;//标识2
  protected ASTStructure structure = null;
  public ASTAdapter(){
    this.root = new ASTRootNode();//初始化根节点
    this.structure = new ASTStructure();
    this.currentNode = this.root;
  }

  public void parse(String jsonQueryStmt){//
    wordSlot = new ArrayList<>();
    //对报文进行清洗
    char[] charCode = jsonQueryStmt.replace(" ", "").trim().toCharArray();
    for (char c : charCode) {
        //解析出第一个元素后才可以将state_0设置为false
        int res = 0;
        res=this.isFlag(c);
        structure.setStructure(res);
        if(res == 0){//未定位到结束符
          //将字符压入单词插槽
          wordSlot.add(c);
        }else{//定位到结束符
          System.out.println(this.concatAryListToString(wordSlot) + " " + c + " " + this.structure.getCurrentStage() + " " + this.structure.getLastStruct());//+ " " + this.structure.getLastStruct());
          words.add(this.concatAryListToString(wordSlot));
          if(this.concatAryListToString(wordSlot).length()!=0) {//单词插槽中数据不为空
            switch (structure.getCurrentStage()) {
              case "table_def": {
                if(structure.getLastStruct().equals("_start")) {
                  ASTTableNode table = new ASTTableNode();
                  table.setIdentify(this.concatAryListToString(wordSlot));
                  this.currentNode.mountSon(table);
                  this.currentNode = table;
//                  System.out.println(this.currentNode.getRole());
                }else if(structure.getLastStruct().equals("field_def")){
                  ASTTableNode table = new ASTTableNode();
                  table.setIdentify(this.concatAryListToString(wordSlot));
                  this.currentNode.mountYounger(table);
                  this.currentNode = table;
                }
              }
              break;
              case "condition_def": {
                ASTConditionNode condition = new ASTConditionNode();
                condition.setExpress(this.concatAryListToString(wordSlot));
                ((ASTTableNode)this.currentNode).mountCondition(condition);
              }
              break;
              case "field_def": {
                if(structure.getLastStruct().equals("field_start") || structure.getLastStruct().equals("table_def")){
                  ASTFieldNode field = new ASTFieldNode();
                  ((ASTTableNode)this.currentNode).mountField(field);
                  field.mountPTable((ASTTableNode) this.currentNode);
                  field.mountParent(this.currentNode);
                  field.setIdentify(this.concatAryListToString(wordSlot));
                  this.currentNode = field;
                }else if(structure.getLastStruct().equals("field_def")){
                  ASTFieldNode field = new ASTFieldNode();
                  field.setIdentify(this.concatAryListToString(wordSlot));
                  this.currentNode.mountYounger(field);
                  field.mountElder(this.currentNode);
                  this.currentNode = field;
                }
              }
              break;
            }
          }else if(this.concatAryListToString(wordSlot).length()==0){//单词插槽为空，节点层级提升
            switch(structure.getCurrentStage()){
              case "field_def": {
//                System.out.println(this.currentNode.getRole());
                if(this.currentNode.getRole().equals("field") || this.currentNode.getRole().equals("table")){//寻找哥哥节点直至其为空后，提升到table节点
                  for(;;){
                    if(this.currentNode.getElder()!=null) {
                      this.currentNode = this.currentNode.getElder();
                    }else{
                      break;
                    }
                  }
                  this.currentNode = this.currentNode.getParent();
                }
                System.out.println(this.currentNode.getToken());
              }break;
            }
          }
          this.wordSlot.clear();
        }
    }

    System.out.println("=========================iterate ast==============================");
    System.out.println(this.root.toString());
    this.iterateAST(this.root.getChild());

  }

  private String checkNodeType(ASTElement node){
    if(node instanceof ASTTableNode){ return "table";
    }else if(node instanceof ASTFieldNode){ return "field";
    }else if(node instanceof ASTRootNode){ return "root";
    }else if(node instanceof ASTConditionNode){ return "condition";
    } else{return "NULL";
    }
  }
  private String arrow_state = "process";
  private String lastState = "NULL";
  private ASTElement lastNode = null;
  protected void iterateAST(ASTElement node){
    if(arrow_state == "process"){
      //table:
      if(node instanceof ASTTableNode){
        this.iterateAST(((ASTTableNode) node).getField());
      }
      //field:
      if(node instanceof ASTFieldNode){
        if(node.getYounger() == null){
          arrow_state = "return";
          this.iterateAST(node.getElder());
        }else{
          this.iterateAST(node.getYounger());
        }
      }
    }else if(arrow_state == "return"){
      //table:
      if(node instanceof ASTTableNode){
        if(node.getElder()==null && node.getYounger() == null && node.getParent() == null) {
        }else if(node.getElder() != null){
          arrow_state = "return";
          this.iterateAST(node.getElder());
        }else if(node.getYounger() != null){
          arrow_state = "process";
          this.iterateAST(node.getYounger());
        }else if(node.getParent() != null){
          arrow_state = "return";
          this.iterateAST(node.getParent());
        }
      }
      //field:
      if(node instanceof ASTFieldNode){
        if(((ASTFieldNode) node).getPtable() != null){
          this.iterateAST(((ASTFieldNode) node).getPtable());
        }else{
          this.iterateAST(node.getElder());
        }
      }

      if(node instanceof ASTRootNode){
        //complete 结束递归
        System.out.println("return root node!");
      }

    }
  }
  protected int isFlag(char input){
    switch(input){
      case '(': {return 1;}//范围标识符，条件范围开始，需要写入pos1
      case ')': {return 2;}//范围标识符，条件范围结束，结束标识需要清空标识1， 标识2，需要写入pos2，写入后需要记录结构
      case '{': {return 3;}//范围标识符，字段范围开始，需要写入pos1
      case '}': {return 4;}//范围标识符，字段范围结束，结束标识需要清空标识1， 标识2，需要写入pos2，，吸入后需要记录结构
      case ':': {return 5;}//别名
//      case '&': {return 6;}//逻辑与
//      case '|': {return 7;}//逻辑或
//      case '!':  {return 8;}//逻辑非
      case ',': {return 6;}//子句结束及子句开始
      default: {return 0;}//默认值
    }
  }
  protected int isNumberReserveFunction(String identify){
    switch(identify){
      case "abs": {return 11;}
      case "sqrt": {return 12;}
      case "mod": {return 13;}
      case "ceil": {return 14;}
      case "floor": {return 15;}
      case "rand": {return 16;}
      case "round": {return 17;}
      case "sign": {return 18;}
      case "pow": {return 19;}
      case "sin": {return 20;}
      case "asin": {return 21;}
      case "cos": {return 22;}
      case "acos": {return 23;}
      case "tan": {return 24;}
      case "atan": {return 25;}
      case "cot": {return 26;}
      default:{return 0;}
    }
  }

  protected int isStrReserveFunction(String input){
    switch(input){
      case "length(": {return 31;}
      case "concat(": {return 32;}
      case "insert(": {return 33;}
      case "lower(": {return 34;}
      case "upper(": {return 35;}
      case "left(": {return 36;}
      case "right(": {return 37;}
      case "trim(": {return 38;}
      case "replace(": {return 39;}
      case "substring(":{return 40;}
      case "reverse(": {return 41;}
      default: return 0;
    }
  }

  protected int isDATEReserveFunction(String input){
    switch(input){
      case "curdate(":{return 51;}
      case "curtime(":{return 52;}
      case "now(": {return 53;}
      case "unix_timestamp(": {return 54;}
      case "from_unixtime(":{return 55;}
      case "month(": {return 56;}
      case "monthname(": {return 57;}
      case "dayname(":{return 58;}
      case "dayofweek(":{return 59;}
      case "week(":{return 60;}
      case "dayofyear(":{return 61;}
      case "dayofmonth(": {return 62;}
      case "year(": {return 63;}
      case "time_to_sec(":{return 64;}
      case "sec_to_time(": {return 65;}
      case "adddate(":{return 66;}
      case "subdate(": {return 67;}
      case "addtime(": {return 68;}
      case "subtime(": {return 69;}
      case "datediff(":{return 70;}
      case "date_format(": {return 71;}
      case "weekday(":{return 72;}
      default: return 0;
    }
  }

  protected int isAggregateReserveFunction(String input){
    switch(input){
      case "max(":{return 81;}
      case "min(":{return 82;}
      case "count(":{return 83;}
      case "sum(":{return 84;}
      case "avg(":{return 85;}
      default: return 0;
    }
  }

  protected String concatAryListToString(ArrayList<Character> ls){
    StringBuilder builder = new StringBuilder(ls.size());
    for(Character ch: ls){
      builder.append(ch);
    }
    return builder.toString();
  }
}
