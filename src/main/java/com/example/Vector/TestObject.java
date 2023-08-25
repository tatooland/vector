package com.example.Vector;

import io.vertx.mysqlclient.MySQLPool;

import java.util.concurrent.atomic.AtomicInteger;

public class TestObject {
  public final static   TestObject testObject =new TestObject();
  public static MySQLPool pool;
  public static volatile AtomicInteger i = new AtomicInteger(0);
//  public static int i = 0;
  public static int cal() throws InterruptedException {
    for(int j = 0; j < 5; j++) {
      Thread.sleep(1000);
      i.getAndIncrement();
//      i++;
      System.out.println(i.get()+"," + Thread.currentThread().getName());
      pool.getConnection().onSuccess(conn->{
        conn.preparedQuery("");
        conn.close();
      });
    }
    return i.get();
//    return i;
  }
}
