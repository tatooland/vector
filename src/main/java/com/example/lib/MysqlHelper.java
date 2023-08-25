package com.example.lib;

import com.example.config.MySQLConfig;
import io.vertx.core.*;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class MysqlHelper extends AbstractVerticle {

  public MysqlHelper(){

  }
  public Future<String> rawSQL(String sqlstatement, Tuple params, Handler handler){
    Future<String> future = Future.future(handler);
    MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    return future;
  }

  @Override
  public void start() {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("127.0.0.1")
      .setDatabase("zoo")
      .setUser("root")
      .setPassword("");
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(2);

  }

}
