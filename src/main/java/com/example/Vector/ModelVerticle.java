package com.example.Vector;
import com.to.core.ann.*;
import com.to.core.base.JSONAdapter;
import com.to.core.base.ast.ASTAdapter;
import com.to.core.base.DataAdapterEncoder;
import com.to.core.base.Vector;
import com.to.core.utils.DataAdapter;
import com.to.core.lib.MySQLHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import java.util.Arrays;
import java.util.Map;



public class ModelVerticle extends Vector {
  @VectorConfig(allowCORS = true, independentRedisPool = true, independentMySqlPool = true, shareRedisPool = false, shareMySqlPool = false, port = 7777)
  public void init() {}

  @Get(path = "/test1")
  public void ModelAdd(RoutingContext ctx) {
    //模型入库
    //数据库操作
    this.getRedis().connect().onSuccess(conn -> {
      RedisAPI.api(conn).get("author").onSuccess(val -> {
        this.retResponse(ctx, val.toString());
      });
    });
  }

  @Get(path = "/test2")
  public void ModelUpdate(RoutingContext ctx) {

    this.getMySQL().getConnection().onSuccess(conn -> {
      conn.preparedQuery("select count(1) from users")
        .execute().onSuccess(rs -> {
          conn.close();
          int amount = 0;
          for (Row r : rs) {
            amount = r.getInteger("count(1)");
          }
          ctx.json(new JsonObject().put("result", amount));
        });
    });

  }

  @Get(path = "/test3")
  public void ModelDel(RoutingContext ctx) {
    ctx.response()
      .putHeader("content-type", "text/plain")
      .end("model delete");
  }

  @Get(path = "/test4")
  public void retResponse(RoutingContext ctx, String val) {
    if (val == null) {
      System.out.println("none");
    } else {
      ctx.response().putHeader("content-type", "text/plain").end(val);
    }
  }

  @Post(path = "/user/regist")
  public void registUser(RoutingContext ctx, @FromJsonParams String name, @FromJsonParams int stuNbr) {
    this.getMySQL().getConnection().onSuccess(conn -> {
      conn.preparedQuery("select count(1) as amount from users")
        .execute();
    });
    ctx.json(new JsonObject().put("n", name).put("no.", stuNbr));
  }

  @Post(path = "/user/jsObj")
  public void JsObjTest(RoutingContext ctx, @FromJsonParams JsonObject numb) {
    ctx.json(numb);
  }

  @Post(path = "/tt1")
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
      if (rowSetAsyncResult.succeeded()) {
        ctx.json(new JsonObject().put("result", "success"));
      } else {
        ctx.json(new JsonObject().put("result", "falt"));
      }
    });
  }

  @Post(path = "/rds")
  public void rdsTest(RoutingContext ctx, @FromJsonParams String key, @FromJsonParams String value) {
    this.getRedis().connect().onSuccess(conn -> {
      RedisAPI.api(conn).set(Arrays.asList(key, value)).onSuccess(result -> {
        conn.close();
        ctx.json(new JsonObject().put("result", result.toString()));
      }).onFailure(err -> {
        conn.close();
        ctx.json(new JsonObject().put("result", err.getMessage()));
      });
    });
  }

  @Post(path = "/rds/get")
  public void rdsGet(RoutingContext ctx, @FromJsonParams String key) {
    this.getRedis().connect().onSuccess(conn -> {
      RedisAPI.api(conn).get(key).onSuccess(result -> {
        conn.close();
        ctx.json(new JsonObject().put("result", result.toString()));
      }).onFailure(err -> {
        ctx.json(new JsonObject().put("result", err.getMessage()));
      });
    });
  }

  @Post(path = "/json/map")
  public void jsonMapTest(RoutingContext ctx, @JsonParam JsonObject jsonObj) {
    MySQLHelper mySQLHelper = new MySQLHelper(this);
    ctx.json(new JsonObject().put("result", "ok"));
    mySQLHelper.Map(jsonObj);
  }

  /*数据协议到映射模板测试-多例*/
  @Post(path = "/dataAdapter/multiple")
  public void multipleData(RoutingContext ctx, @JsonParam JsonObject jsonObject) {
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(jsonObject.toString());
    JsonObject ret = new JsonObject();
    mapping.forEach((key, value) -> {
      ret.put(key, value);
    });
    ctx.json(ret);
  }

  @Post(path = "/dataAdapter/single")
  public void singleData(RoutingContext ctx, @JsonParam JsonObject jsonObject) {
    DataAdapterEncoder encoder = new DataAdapterEncoder();
    Map<String, String> mapping = encoder.encode(jsonObject.toString());
    JsonObject ret = new JsonObject();
    mapping.forEach((key, value) -> {
      ret.put(key, value);
    });
    ctx.json(ret);
  }

  //数据协议测试
  @Post(path = "/dataAdapter/MapTask")
  public void genMap(RoutingContext ctx, @FromJsonParams String document, @FromJsonParams JsonObject protocol) {
    DataAdapter adapter = new DataAdapter(this);
    adapter.genMapping(document, protocol).onSuccess(res -> {
      ctx.json(new JsonObject().put("result", res.toString()));
    });
  }

  @Post(path = "/Rds/promise")
  public void promiseTest(RoutingContext ctx, @FromJsonParams String document) {
    DataAdapter adapter = new DataAdapter(this);
    adapter.queryMapping(document).onSuccess(res -> {

      System.out.println(new JsonObject(res.toString()).getString("vector_framework_data_type"));
      ctx.json(new JsonObject(res.toString()));
    });
  }

  @Post(path = "/mapping/query")
  public void queryMapping(RoutingContext ctx, @FromJsonParams String document) {
    DataAdapter adapter = new DataAdapter(this);
    adapter.queryMapping(document)
      .onSuccess(res -> {
        ctx.json(new JsonObject(res.toString()));
      });
  }

  @Post(path = "/szj/weekly")
  public void sqlTest(RoutingContext ctx, @FromJsonParams String dateTime) {
    this.getMySQL().getConnection().onSuccess(conn -> {
      conn.preparedQuery("select \n" +
          "time_cd,        #报表统计日\n" +
          "lant_name,      #分公司名称\n" +
          "lst_week_total, #上周双智家\n" +
          "lst_week_add,   #上周增量双智家\n" +
          "lst_week_store, #上周存量双智家\n" +
          "cur_week_total, #本周双智家\n" +
          "cur_week_add,   #本周增量双智家\n" +
          "cur_week_store  #本周存量双智家\n" +
          "from rpt_szj_add_store_weekly  \n" +
          "where time_cd =? ;")
        .execute(Tuple.of(dateTime))
        .onSuccess(RowSet->{
          JsonArray ary = new JsonArray();
          for (Row row : RowSet) {
            JsonObject temp = new JsonObject();
            temp.put("time_cd",row.getString("time_cd"));
            temp.put("lant_name",row.getString("lant_name"));
            temp.put("lst_week_total",row.getString("lst_week_total"));
            temp.put("lst_week_add",row.getString("lst_week_add"));
            temp.put("lst_week_store",row.getString("lst_week_store"));
            temp.put("cur_week_total",row.getString("cur_week_total"));
            temp.put("cur_week_add",row.getString("cur_week_add"));
            temp.put("cur_week_store",row.getString("cur_week_store"));
            ary.add(temp);
          }
            ctx.json(new JsonObject().put("result", ary));
        });
    });
  }

  @Post(path="/szj/test")
  public void szjTest(RoutingContext ctx, @FromJsonParams String dt, @FromJsonParams String document){
    String sql = "select \n" +
      "lant_name,      #分公司名称\n" +
      "lst_week_total, #上周双智家\n" +
      "lst_week_add,   #上周增量双智家\n" +
      "lst_week_store, #上周存量双智家\n" +
      "cur_week_total, #本周双智家\n" +
      "cur_week_add,   #本周增量双智家\n" +
      "cur_week_store  #本周存量双智家\n" +
      "from rpt_szj_add_store_weekly  \n" +
      "where time_cd =? ;";
    MySQLHelper msh = new MySQLHelper(this);

    Future<String> result = msh.RawSQLExecute(sql, Tuple.of(dt), document);
    result.onSuccess(Rs->{
      ctx.json(new JsonObject().put("result", new JsonObject(Rs.toString())));
    });
  }

  @Post(path="/rhfzl/table")
  public void rhfzl(RoutingContext ctx, @FromJsonParams String dt, String document){
    String sql = "select (case when aa.offer_name is null then bb.offer_name else aa.offer_name end) 融合套餐,\n" +
      "ifnull(aa.合计,'0') 本周合计,ifnull(bb.合计,'0') 上周合计, round((aa.合计/bb.合计-1)*100,2) \"本周环比(%)\",\n" +
      "ifnull(aa.城市,'0') 本周城市,ifnull(bb.城市,'0') 上周城市,round((aa.城市/bb.城市-1)*100,2) \"本周城市环比(%)\",\n" +
      "ifnull(aa.农村,'0') 本周农村,ifnull(bb.农村,'0') 上周农村,round((aa.农村/bb.农村-1)*100,2)  \"本周农村环比(%)\",\n" +
      "ifnull(aa.商圈,'0') 本周商圈,ifnull(bb.商圈,'0') 上周商圈,round((aa.商圈/bb.商圈-1)*100,2)  \"本周商圈环比(%)\",\n" +
      "ifnull(aa.直营,'0') 本周直营,ifnull(bb.直营,'0') 上周直营,round((aa.直营/bb.直营-1)*100,2)  \"本周直营环比(%)\"\n" +
      " from \n" +
      "-- 本周发展\n" +
      "(select apd.offer_name,count(apd.prod_inst_id) 合计,\n" +
      "apd.offer_id,\n" +
      "sum(case apd.area_type when '城市' then 1 else 0 end) 城市,\n" +
      "sum(case apd.area_type when '农村' then 1 else 0 end) 农村,\n" +
      "sum(case apd.area_type when '商圈' then 1 else 0 end) 商圈,\n" +
      "sum(case apd.area_type when '直营厅' then 1 else 0 end) 直营\n" +
      " from iwc_exp_app_pro_spring_deve_d  apd where apd.product_type='KD'\n" +
      "and apd.day_id>=date_format(date_add('20230821',INTERVAL -7 DAY),'%Y%m%d') \n" +
      "and apd.day_id<=date_format(date_add('20230821',INTERVAL -1 DAY),'%Y%m%d')\n" +
      "-- 剔除校园宽带\n" +
      "and apd.product_id not in ('6610230000','6600119000')\n" +
      "-- 剔除单宽\n" +
      "and apd.is_rh_flag='融合'\n" +
      "-- 渠道类型\n" +
      "and apd.area_type in ('城市','农村','直营厅','商圈')\n" +
      "group by apd.offer_name,apd.offer_id) aa right join \n" +
      "-- 上周发展\n" +
      "(select apd.offer_name,count(apd.prod_inst_id) 合计,\n" +
      "apd.offer_id,\n" +
      "sum(case apd.area_type when '城市' then 1 else 0 end) 城市,\n" +
      "sum(case apd.area_type when '农村' then 1 else 0 end) 农村,\n" +
      "sum(case apd.area_type when '商圈' then 1 else 0 end) 商圈,\n" +
      "sum(case apd.area_type when '直营厅' then 1 else 0 end) 直营\n" +
      " from iwc_exp_app_pro_spring_deve_d  apd where apd.product_type='KD'\n" +
      "and apd.day_id>=date_format(date_add('?',INTERVAL -14 DAY),'%Y%m%d') \n" +
      "and apd.day_id<=date_format(date_add('?',INTERVAL -8 DAY),'%Y%m%d')\n" +
      "-- 剔除校园宽带\n" +
      "and apd.product_id not in ('6610230000','6600119000')\n" +
      "-- 剔除单宽\n" +
      "and apd.is_rh_flag='融合'\n" +
      "-- 渠道类型\n" +
      "and apd.area_type in ('城市','农村','直营厅','商圈')\n" +
      "group by apd.offer_name,apd.offer_id) bb\n" +
      "on aa.offer_id=bb.offer_id\n" +
      "order by aa.合计 desc;";
    MySQLHelper msh = new MySQLHelper(this);
    Future<String> result = msh.RawSQLExecute(sql, Tuple.of(dt), document);
    result.onSuccess(Rs->{
      ctx.json(new JsonObject().put("result", new JsonObject(Rs.toString())));
    });
  }
  @Post(path="/jsq")
  public void jsonQuery(RoutingContext ctx, @FromJsonParams String qStmt){
    long startTime=System.currentTimeMillis();
    System.out.println(qStmt);
    ASTAdapter ast = new ASTAdapter();
    ast.parse(qStmt);
    long endTime = System.currentTimeMillis();
    System.out.println("ast解析时间" +
      "：" + (endTime-startTime) + "ms");
    ctx.json(qStmt);
  }
  @Post(path="/json2code")
  public void jsonToCode(RoutingContext ctx, @FromJsonParams String jsonString){
    JSONAdapter adapter = new JSONAdapter();
    adapter.parse(jsonString);

  }
  @Post(path="/wangchaoceshi")
  public void J(RoutingContext ctx, @FromJsonParams String hello){
    ctx.json(hello);
  }
}

