package com.to.core.base;

import com.to.core.ann.Get;
import com.to.core.ann.Post;
import com.to.core.ann.VectorConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Vector extends AbstractVerticle {
  private int port = 0;
  private boolean useGlobalMySQLConnection = false;
  private boolean useLocalMySQLConnection = false;
  private boolean useGlobalRedisConnection = false;
  private boolean useLocalRedisConnection = false;

  protected MySQLPool mysqlLocalPool = null;
  protected MySQLPool mysqlGlobalPool = null;

  public Vector(){
    //
  }
  @Override
  public void  start() {
    //provide router for http path registing
    Router router = Router.router(vertx);
    //遍历具备http注解的方法,并获取参数
    for (Method method : this.getClass().getMethods()) {
      if(method.isAnnotationPresent(Get.class)){//获取进行了http Get请求声明的子类成员方法
        Get methodAnn = method.getAnnotation(Get.class);
        String path = methodAnn.path();
        router.get(path).handler(ctx->{
          try {
            method.invoke(this,ctx);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        });
      }else if(method.isAnnotationPresent(Post.class)){//获取进行了http  Post请求声明的子类成员方法
        Post methodAnn = method.getAnnotation(Post.class);
        String path = methodAnn.path();
        router.post(path).handler(ctx->{
          try {
            method.invoke(this,ctx);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        });
      }else if(method.isAnnotationPresent(VectorConfig.class)){//http服务器及组件配置参数获取
        VectorConfig vc = method.getAnnotation(VectorConfig.class);
        this.port = vc.port();
        this.useGlobalMySQLConnection = vc.shareMySqlPool();
        this.useGlobalRedisConnection = vc.shareRedisPool();
        this.useLocalMySQLConnection = vc.independentMySqlPool();
        this.useLocalRedisConnection = vc.independentRedisPool();
      }
    }

    //http服务器启动
    Method[] methods = this.getClass().getDeclaredMethods();
    vertx.createHttpServer().requestHandler(router).listen(this.port);


    //MySQL数据库连接池初始化
    if(this.useGlobalMySQLConnection){
      //使用共享连接池
      this.deployGlobalMySQLPool();
    }

    if(this.useLocalMySQLConnection){
      //使用本地连接池
      this.createLocalMySQLPool();
    }

    //Redis连接池初始化
    if(this.useGlobalRedisConnection){
      //使用共享redis连接池
      this.deployGlobalRedisPool();
    }

    if(this.useLocalRedisConnection){
      //使用本地redis连接池
      this.createLocalRedisPool();
    }
  }

  @Override
  public void stop() {
    if(this.useLocalMySQLConnection){

    }

    if(this.useLocalRedisConnection){

    }
  }

//部署共享mysql连接池
  protected void deployGlobalMySQLPool() {
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool")==null){//判断共享连接池是否存在,不存在则创建
      System.out.println("global mysql pool not set");
      MySQLConnectOptions connectOptions = new MySQLConnectOptions()
        .setHost("localhost")
        .setPort(3306)
        .setDatabase("zoo")
        .setPassword("");
      PoolOptions poolOptions = new PoolOptions().setMaxSize(2);
      MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);
      vertx.sharedData().getLocalMap("vector-pool").put("vector-mysql-pool", pool);
    }
  }
//部署共享redis连接池
  protected void deployGlobalRedisPool() {

  }
//创建本地mysql连接池
  protected void createLocalMySQLPool() {//创建依附于当前vector的mysql连接池，当vector释放后连接池被释放
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("zoo")
      .setPassword("");
    PoolOptions poolOptions = new PoolOptions().setMaxSize(2);
    this.mysqlLocalPool = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }
//获取本地mysql连接池连接
  private MySQLConnection getConn(){
    if(this.mysqlLocalPool != null){
        return (MySQLConnection) this.mysqlLocalPool.getConnection();
    }else{
      throw new NullPointerException("mysql pool cannot be null");
    }
  }
//获取共享mysql连接池连接
  private MySQLConnection getGlobalConn() {
    //获取共享mysql链接池
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool") != null){
      MySQLPool pool = (MySQLPool) vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool");
      return (MySQLConnection) pool.getConnection();
    }else{
      throw new NullPointerException();
    }
  }
//创建本地redis连接池
  protected void createLocalRedisPool() {

  }


}
