package com.to.core.lib;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.HashMap;
import java.util.Map;

public class MySQLHelper {
  private MySQLPool pool;
  public MySQLHelper(MySQLPool pool){
    if(pool != null) {
      this.pool = pool;
    }else{
      throw new NullPointerException();
    }
  }
  public MySQLHelper() {

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
  private String blockingSQL;
  private Tuple blockingTuple;
  public MySQLHelper BlockingQuery(String sql){
    this.blockingSQL = sql;
    return this;
  }
  public MySQLHelper SetParams(Tuple params){
    this.blockingTuple = params;
    return this;
  }

  public MySQLHelper Map(JsonObject rsObj){
    System.out.println(rsObj.toString());
    //取出 模板参数（以${}$进行声明的)
    Map<String, String> tplMapEntry = new HashMap<>();
    this.parseJson(rsObj.toString(), tplMapEntry, 0);
//    for (Map.Entry<String, Object> stringObjectEntry : rsObj) {
//      System.out.println(stringObjectEntry.getKey()+", " + stringObjectEntry.getValue());
//    }
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
      conn.preparedQuery(this.blockingSQL)
        .execute(this.blockingTuple)
        .onSuccess(rs->{
          promise.complete(rs);
        });
    });
    return promise.future();
  }
  public Future<RowSet> RawSQLExecute(String sql, Tuple tuple) {
    Promise promise = Promise.promise();
    if(tuple != null) {
      this.pool.getConnection().onSuccess(conn -> {
        conn.preparedQuery(sql).execute(tuple)
          .onComplete(ar -> {
            conn.close();
            if (ar.succeeded()) {
              promise.complete(ar.result());
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
              promise.complete(ar.result());
              System.out.println("sql execute success");
            } else {
              System.out.println(ar.cause().getMessage());
            }
          });
      });
    }
    return promise.future();
  }
}
/*
 * future = sqlHelper.query(sql).
 * bindParams(fieldsList)
 * .excute()
 * .query(sql).bindParams().map(templateId)
 * //future.get()
 * int i = 0;
 * future.onSucess(result->{
 *     i = result;
 *      do(i)
 * })
 * do=i=>{}
 * */
