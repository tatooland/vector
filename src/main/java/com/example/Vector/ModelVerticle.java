package com.example.Vector;
import com.to.core.ann.*;
import com.to.core.base.DataAdapterEncoder;
import com.to.core.base.Vector;
import com.to.core.utils.DataAdapter;
import com.to.core.lib.MySQLHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class ModelVerticle extends Vector {
  @VectorConfig(independentRedisPool = true, independentMySqlPool = true, shareRedisPool = false, shareMySqlPool = false,port=7777)
  public void init(){}
  @Get(path="/test1")
  public void ModelAdd(RoutingContext ctx){
    //模型入库
    //数据库操作

    this.getRedis().connect().onSuccess(conn -> {
      RedisAPI.api(conn).get("author").onSuccess(val -> {
        this.retResponse(ctx, val.toString());
      });
    });
  }
  @Get(path="/test2")
  public void ModelUpdate(RoutingContext ctx){

      this.getMySQL().getConnection().onSuccess(conn -> {
        conn.preparedQuery("select count(1) from users")
          .execute().onSuccess(rs->{
            conn.close();
            int amount = 0;
            for (Row r : rs) {
              amount = r.getInteger("count(1)");
            }
            ctx.json(new JsonObject().put("result", amount));
          });
      });

  }
  @Get(path="/test3")
  public void ModelDel(RoutingContext ctx){
    ctx.response()
      .putHeader("content-type", "text/plain")
      .end("model delete");
  }
  @Get(path="/test4")
  public void retResponse(RoutingContext ctx, String val){
    if(val == null){
      System.out.println("none");
    }else {
      ctx.response().putHeader("content-type", "text/plain").end(val);
    }
  }

  @Post(path="/user/regist")
  public void registUser(RoutingContext ctx, @FromJsonParams String name, @FromJsonParams int stuNbr){
    this.getMySQL().getConnection().onSuccess(conn->{
      conn.preparedQuery("select count(1) as amount from users")
        .execute();
    });
    ctx.json(new JsonObject().put("n", name).put("no.", stuNbr));
  }

  @Post(path="/user/jsObj")
  public void JsObjTest(RoutingContext ctx, @JsonParam JsonObject obj){
    ctx.json(obj);
  }

  @Post(path="/tt1")
  public void tt1(RoutingContext ctx, @FromJsonParams String username, @FromJsonParams String password) {
    String sql =
      "CREATE TABLE `shdx_order` (\n" +
      "  `id` int NOT NULL AUTO_INCREMENT,\n" +
      "  `serial_number` varchar(32) NOT NULL,\n" +
      "  `name` varchar(20) NOT NULL,\n" +
      "  `idcard` varchar(20) NOT NULL,\n" +
      "  `contact_mobile` varchar(11) NOT NULL,\n" +
      "  `installed_address` varchar(512) NOT NULL,\n" +
      "  `saleid` varchar(16) NOT NULL,\n" +
      "  `storeid` varchar(16) NOT NULL,\n" +
      "  `person1id` varchar(16) NOT NULL,\n" +
      "  `province` varchar(50) NOT NULL,\n" +
      "  `city` varchar(50) NOT NULL,\n" +
      "  `county` varchar(50) NOT NULL,\n" +
      "  `channel_id` varchar(32) NOT NULL,\n" +
      "  `orderid` varchar(128) DEFAULT 'no_data',\n" +
      "  `create_time` varchar(21) NOT NULL,\n" +
      "  `ordermsg` varchar(512) NOT NULL DEFAULT 'no_data',\n" +
      "  `route_info` varchar(1024) NOT NULL DEFAULT 'no_data',\n" +
      "  `state_info` varchar(1024) NOT NULL DEFAULT 'no_data',\n" +
      "  `freight_code` varchar(128) NOT NULL DEFAULT 'no_data',\n" +
      "  `acc_nbr` varchar(32) DEFAULT 'no_data',\n" +
      "  PRIMARY KEY (`id`),\n" +
      "  KEY `serial_number` (`serial_number`),\n" +
      "  KEY `orderid` (`orderid`)\n" +
      ") ENGINE=InnoDB AUTO_INCREMENT=131967 DEFAULT CHARSET=utf8";
    MySQLHelper mySQLHelper = new MySQLHelper(this.getMySQL());
    Future<RowSet> f = mySQLHelper.RawSQLExecute(sql, null);
    f.onComplete(rowSetAsyncResult -> {
      if(rowSetAsyncResult.succeeded()){
        ctx.json(new JsonObject().put("result", "success"));
      }else{
        ctx.json(new JsonObject().put("result", "falt"));
      }
    });
  }
  @Post(path="/rds")
  public void rdsTest(RoutingContext ctx,@FromJsonParams String key, @FromJsonParams String value){
    this.getRedis().connect().onSuccess(conn->{
      RedisAPI.api(conn).set(Arrays.asList(key, value)).onSuccess(result->{
        conn.close();
        ctx.json(new JsonObject().put("result", result.toString()));
      }).onFailure(err->{
        conn.close();
        ctx.json(new JsonObject().put("result", err.getMessage()));
      });
    });
  }

  @Post(path="/rds/get")
  public void rdsGet(RoutingContext ctx, @FromJsonParams String key){
    this.getRedis().connect().onSuccess(conn->{
      RedisAPI.api(conn).get(key).onSuccess(result->{
        conn.close();
        ctx.json(new JsonObject().put("result", result.toString()));
      }).onFailure(err ->{
        ctx.json(new JsonObject().put("result", err.getMessage()));
      });
    });
  }

  @Post(path="/json/map")
  public void jsonMapTest(RoutingContext ctx, @JsonParam JsonObject jsonObj){
    MySQLHelper mySQLHelper = new MySQLHelper();
    ctx.json(new JsonObject().put("result", "ok"));
    mySQLHelper.Map(jsonObj);
  }

  /*数据协议到映射模板测试-多例*/
  @Post(path="/dataAdapter/multiple")
  public void multipleData(RoutingContext ctx, @JsonParam JsonObject jsonObject){
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(jsonObject.toString());
    JsonObject ret = new JsonObject();
    mapping.forEach((key, value) -> {
      ret.put(key, value);
    }) ;
    ctx.json(ret);
  }
  @Post(path="/dataAdapter/single")
  public void singleData(RoutingContext ctx, @JsonParam JsonObject jsonObject){
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(jsonObject.toString());
    JsonObject ret = new JsonObject();
    mapping.forEach((key, value)->{
      ret.put(key, value);
    });
    ctx.json(ret);
  }

  @Post(path="/dataAdapter/MapTask")
  public void genMap(RoutingContext ctx, @FromJsonParams String protocolName, @FromJsonParams JsonObject protocol){
    DataAdapter adapter = new DataAdapter(this);
    String result = adapter.genMapping(protocolName, protocol);
    ctx.json(new JsonObject().put("result", result));
  }
}
