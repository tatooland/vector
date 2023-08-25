package com.to.core.base;

import com.example.config.RedisConfig;
import com.to.core.ann.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.PoolOptions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;


public class Vector extends AbstractVerticle {
  private int port = 0;
  private boolean useGlobalMySQLConnection = false;
  private boolean useLocalMySQLConnection = false;
  private boolean useGlobalRedisConnection = false;
  private boolean useLocalRedisConnection = false;

  protected MySQLPool mysqlLocalPool = null;


  protected Redis redisLocalPool = null;

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
        //遍历方法的入参列表
        Post methodAnn = method.getAnnotation(Post.class);
        String path = methodAnn.path();
        router.post(path).handler(ctx->{
          if(ctx.request().getHeader("Content-Type").equals("application/json")) {
            ctx.request().bodyHandler(bodyHandler-> {
              ArrayList<Object> argList = new ArrayList<>();
              argList.add(ctx);
              JsonObject jsObj = bodyHandler.toJsonObject();
              for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(FromJsonParams.class)) {
                  String key = parameter.getName();
                  switch (parameter.getType().getName()) {
                    case "int": {
                      argList.add(jsObj.getInteger(key));
                    }
                    break;
                    case "String": {
                      argList.add(jsObj.getString(key));
                    }
                    break;
                    case "long": {
                      argList.add(jsObj.getLong(key));
                    }
                    break;
                    case "boolean": {
                      argList.add(jsObj.getBoolean(key));
                    }
                    break;
                    case "JsonObject": {
                      argList.add(jsObj.getJsonObject(key));
                    }
                    break;
                    case "JsonArray":{
                      argList.add(jsObj.getJsonArray(key));
                    }break;
                    default:
                      argList.add(jsObj.getString(key));
                  }
                }else if(parameter.isAnnotationPresent(JsonParam.class)){
                  argList.add(jsObj);
                }
              }

              int argLength = argList.size();
              try {
                method.invoke(this, (Object[])argList.toArray(new Object[argLength]));
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            });
          }else{
            throw new IllegalArgumentException("capture params in json request must be json format");
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
      releaseLocalMySQLPool();
    }

    if(this.useLocalRedisConnection){
      releaseLocalRedisPool();
    }
  }

//部署共享mysql连接池
  protected void deployGlobalMySQLPool() {
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool")==null){//判断共享连接池是否存在,不存在则创建
      System.out.println("global mysql pool not ready");
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
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-redis-pool")==null){
      System.out.println("global redis pool not ready");
      RedisOptions options = new RedisOptions().setConnectionString("redis://localhost:6379");
      Redis pool = Redis.createClient(vertx, options);
      vertx.sharedData().getLocalMap("vector-pool").put("vector-redis-pool", pool);
    }
  }
//创建本地mysql连接池
  protected void createLocalMySQLPool() {//创建依附于当前vector的mysql连接池，当vector释放后连接池被释放
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("zoo")
      .setPassword("()<>JK2019T^^km");
    PoolOptions poolOptions = new PoolOptions().setMaxSize(2);
    this.mysqlLocalPool = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }
//获取本地mysql连接池连接
  public MySQLPool getMySQL(){
    if(this.mysqlLocalPool != null){
        return  this.mysqlLocalPool;
    }else{
      throw new NullPointerException("mysql pool cannot be null");
    }
  }
//获取共享mysql连接池连接
  public MySQLPool getSharedMySQL() {
    //获取共享mysql链接池
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool") != null){
      MySQLPool pool = (MySQLPool) vertx.sharedData().getLocalMap("vector-pool").get("vector-mysql-pool");
      return pool;
    }else{
      throw new NullPointerException();
    }
  }
//创建本地redis连接池
  protected void createLocalRedisPool() {
    RedisOptions options = new RedisOptions().setConnectionString("redis://localhost:6379");
     redisLocalPool = Redis.createClient(vertx, options);
  }
//获取本地redis对象
  public Redis getRedis(){
    return this.redisLocalPool;
  }
//获取共享redis对象
  public Redis getSharedRedis(){
    if(vertx.sharedData().getLocalMap("vector-pool").get("vector-redis-pool") != null){
      return (Redis) vertx.sharedData().getLocalMap("vector-pool").get("vector-redis-pool");
    }else{
      throw new NullPointerException();
    }
  }
  //释放本地mysql连接池
  public void releaseLocalMySQLPool() {
    if(this.mysqlLocalPool != null){
      this.mysqlLocalPool.close();
    }
  }
  //释放本地redis对象
  public void releaseLocalRedisPool() {
    if(this.redisLocalPool != null){
      this.redisLocalPool.close();
    }
  }
}
