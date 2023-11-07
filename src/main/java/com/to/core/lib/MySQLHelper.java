package com.to.core.lib;

import com.to.core.base.Vector;
import com.to.core.utils.DataAdapter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MySQLHelper {
  private MySQLPool pool;
  private Vector vector;
  public MySQLHelper(MySQLPool pool){
    if(pool != null) {
      this.pool = pool;
    }else{
      throw new NullPointerException();
    }
  }
  public MySQLHelper(Vector vector) {
    this.vector = vector;
    this.pool = this.vector.getMySQL();
  }
  public void execute(String sql, Tuple tuple, Handler<RowSet<Row>> handler){
    if(tuple != null) {
      pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute(tuple).onSuccess(rs -> {
          handler.handle(rs);
          conn.close();
        }).onComplete(ar -> {
          conn.close();
        }).onFailure(throwable -> {
          System.out.println(throwable.getMessage());
          conn.close();
        });
      });
    }else{
      pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute().onSuccess(rs -> {
          handler.handle(rs);
          conn.close();
        }).onComplete(ar -> {
          conn.close();
        }).onFailure(throwable -> {
          System.out.println(throwable.getMessage());
          conn.close();
        });
      });
    }
  }
  private String SQL;
  private Tuple Tuple;

  public MySQLHelper CREATE(String table){
    return this;
  }
  public MySQLHelper SELECT(String table, String ...fields){//000
    SQL = "SELECT ";
    for (String field : fields) {
      SQL += field + ",";
    }
    SQL = SQL.substring(SQL.length()-1);
    SQL += " FROM " + table;
    return this;
  }
  public MySQLHelper INSERT(String table, String ...fields){//001
    SQL = "INSERT INTO " + table + "(";
    String qM = "";
    for (String field : fields) {
      SQL += field+",";
      qM += ",";
    }
    SQL = SQL.substring(SQL.length()-1) + ") VALUES(" + qM.substring(qM.length()-1) + ")";
    return this;
  }
  public MySQLHelper UPDATE(String table, String ...fields) {//010
    SQL = "UPDATE " + table + " ";
    for (String field : fields) {
      SQL += field + "=?,";
    }
    SQL = SQL.substring(SQL.length()-1);
    return this;
  }
  public MySQLHelper DELETE(String table) {//011
    SQL = "DELETE FROM " + table + " ";
    return this;
  }

  public MySQLHelper WHERE(String ...conditions){
    for (String condition : conditions) {
      SQL += condition;
    }
    return this;
  }

  public MySQLHelper LIMIT(){
    return this;
  }
  public MySQLHelper GROUPBY(){
    return this;
  }

  public MySQLHelper ORDERBY(){
    return this;
  }

  public MySQLHelper LEFT_JOIN(String table, String ...on){
    return this;
  }



  public MySQLHelper SET(String ...params) {
    return this;
  }

  public MySQLHelper FROM(String table) {
    return this;
  }




  public MySQLHelper AND(){
    return this;
  }

  public MySQLHelper OR() {
    return this;
  }
  public MySQLHelper BindParams(Tuple params){
    this.Tuple = params;
    return this;
  }


  public String execute(String tplName) {
    String result = null;
    System.out.println(SQL);
    return result;
  }

  public MySQLHelper Map(JsonObject rsObj){
    System.out.println(rsObj.toString());
    //取出 模板参数（以${}$进行声明的)
    Map<String, String> tplMapEntry = new HashMap<>();
    this.parseJson(rsObj.toString(), tplMapEntry, 0);
    String jsonTpl = rsObj.toString();
    return this;
  }
  public void parseJson(String jsonStr, Map<String, String> tplMapEntry, int start){
    if (jsonStr.indexOf("${", start)!=-1) {
      String preFix = jsonStr.substring(start, jsonStr.indexOf("${", start));
      start = jsonStr.indexOf("${", start);
      int end = jsonStr.indexOf("}$", start);
      String tplKey = jsonStr.substring(start+2, end);
      tplMapEntry.put(tplKey, preFix);
      System.out.println(tplKey + ", " + preFix);
      this.parseJson(jsonStr, tplMapEntry, end+2);
    }
  }
  public Future<Object> execute(){
    Promise promise = Promise.promise();
    Future<JsonObject> future = promise.future();
    this.pool.getConnection().onSuccess(conn->{
      conn.preparedQuery(this.SQL)
        .execute(this.Tuple)
        .onSuccess(rs->{
          promise.complete(rs);
        });
    });
    return promise.future();
  }
  public Promise executeResult;
  public Future<RowSet> RawSQLExecute(String sql, Tuple tuple) {
    executeResult = Promise.promise();
    executeResult.future().onFailure(throwable -> {
      System.out.println("get sql execute result error");
    });
    if(tuple != null) {
      this.pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute(tuple)
          .onComplete(ar -> {
            conn.close();
            if (ar.succeeded()) {
              executeResult.complete(ar.result());
              System.out.println("sql execute success");
            } else {
              System.out.println(ar.cause().getMessage());
            }
          });
      });
    } else{
      this.pool.getConnection().onSuccess(conn -> {
        conn.query(sql).execute()
          .onComplete(ar -> {
            conn.close();
            if (ar.succeeded()) {
              executeResult.complete(ar.result());
              System.out.println("sql execute success");
            } else {
              System.out.println(ar.cause().getMessage());
            }
          });
      });
    }
    return executeResult.future();
  }
  public Future<String> RawSQLExecute(String sql, Tuple tuple, String document){
    Promise promise = Promise.promise();
    if(tuple != null){
      this.pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute(tuple).onComplete(ar->{
          conn.close();
          if(ar.succeeded()){
            DataAdapter adapter = new DataAdapter(this.vector);
            adapter.queryMapping(document).onSuccess(res->{
              long startTime=System.currentTimeMillis();
              JsonObject mapJson = new JsonObject(res.toString());
              LinkedHashMap<String, String> mapping = new LinkedHashMap<String, String>();
              mapJson.getMap().forEach((k, v )->{
                mapping.put(k, (String)v);
              });
              String result = adapter.fetchData(ar.result(), mapping);
              promise.complete(result);
              long endTime = System.currentTimeMillis();
              System.out.println("映射模板2LinkedHaspMap运行时间：" + (endTime-startTime) + "ms");
            });
          }else{
            System.out.println(ar.cause().getMessage());
          }
        });
      });
    }else {
      this.pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute().onSuccess(rs -> {

        }).onComplete(ar -> {
          conn.close();
          if (ar.succeeded()) {

          } else {
            System.out.println(ar.cause().getMessage());
          }
        });
      });
    }
    return promise.future();
  }
  public Future<String> simpleQuery(JsonObject jsonObject){
    Promise promise = Promise.promise();
    ArrayList<String> keys = new ArrayList<>();
    String table = jsonObject.getString("table");
    String op = jsonObject.getString("op");
    String condition = jsonObject.getString("condition");
    jsonObject.getMap().forEach((k,v)->{
      if(!k.equals("table") && !k.equals("op") && !k.equals("condition")) {
        keys.add(k);
      }
    });
    String sql = "";
    for (String key : keys) {

    }
//    this.pool.getConnection().compose(conn->{
//
//    });
    return promise.future();
  }
}

