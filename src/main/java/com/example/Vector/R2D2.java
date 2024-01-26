package com.example.Vector;

import com.to.core.ann.FromJsonParams;
import com.to.core.ann.Get;
import com.to.core.ann.Post;
import com.to.core.ann.VectorConfig;
import com.to.core.base.Vector;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class R2D2 extends Vector {
  @VectorConfig(allowCORS = true, independentMySqlPool = true, independentRedisPool = true, shareRedisPool = false, shareMySqlPool = false, port = 7778)
  public void init() {}
  @Post(path="/model/build")
  public void addModel(RoutingContext ctx, @FromJsonParams String modelName,
                       @FromJsonParams String modelDefine, @FromJsonParams String modelInfo){
    SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    this.getMySQL().getConnection().onSuccess(conn -> {
      conn.preparedQuery(modelDefine)
        .execute()
        .onSuccess(rs->{
          conn.preparedQuery("insert into meta(model_name, model_info, create_time) values(?, ?, ?)")
            .execute(Tuple.of(modelName, modelInfo, sdf.format(date)))
            .onSuccess(meta_rs->{
                conn.close();
                ctx.json(new JsonObject().put("result", "success"));
            })
            .onFailure(metaExp->{
              System.out.println(metaExp.getMessage());
              conn.close();
              ctx.json(new JsonObject().put("result", "failure"));
            });
        })
        .onFailure(throwable -> {
          System.out.println(throwable.getMessage());
          conn.close();
          ctx.json(new JsonObject().put("result", "failure"));
        });
    });
  }
  @Post(path="/model/info/qry")
  public void queryModelInfo(RoutingContext ctx, @FromJsonParams JsonObject condition, @FromJsonParams String modelName){
    String stmt = "select * from meta ";
    if(condition.containsKey("t1") && condition.containsKey("t2")){
      this.getMySQL().getConnection().onSuccess(conn->{
        conn.preparedQuery("select * from meta where date_format(create_time, '%Y-%m-%d ') between ? and ?")
          .execute(Tuple.of(condition.getString("t1"), condition.getString("t2")))
          .onSuccess(rs -> {
            JsonArray models = new JsonArray();
            for(Row r : rs){
              JsonObject model = new JsonObject();
              model.put("model_name", r.getString("model_name")).put("model_info", r.getString(""));
              models.add(model);
            }
            conn.close();
            ctx.json(models);
          }).onFailure(err->{
            conn.close();
            ctx.json(null);
          });
      });
    }else if(condition.containsKey("t1") && !condition.containsKey("t2")){
        this.getMySQL().getConnection().onSuccess(conn->{
          conn.preparedQuery("select * from meta where date_format(create_time, '%Y-%m-%d ')=?")
            .execute(Tuple.of(condition.getString("t1")))
            .onSuccess(rs -> {
              JsonArray models = new JsonArray();
              for(Row r : rs){
                JsonObject model = new JsonObject();
                model.put("model_name", r.getString("model_name")).put("model_info", r.getString(""));
                models.add(model);
              }
              conn.close();
              ctx.json(models);
            }).onFailure(err->{
              conn.close();
              ctx.json(null);
            });
        });
    }
  }


  @Post(path="/screen/sales")
  public void querySales(RoutingContext ctx){
    this.getMySQL().getConnection().onSuccess(conn -> {
      conn.preparedQuery("select count(1) as amount from iwc_dm_wid_prd_prod_inst_d_use")
        .execute()
        .onSuccess(rs->{
          for(Row row: rs){
            System.out.println(row.getInteger("amount"));
          }
          conn.close();
          ctx.json(new JsonObject().put("result", "success"));
        }).onFailure(throwable -> {
          conn.close();
          ctx.json(new JsonObject().put("result", "failure").put("msg", throwable.getMessage()));
        });
    });
  }
}
