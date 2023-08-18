package com.example.lib;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyAbstractVerticle extends AbstractVerticle {
  @Override
  public void  start() {
    Router router = Router.router(vertx);
    for (Route route : Router.router(vertx).getRoutes()) {
      System.out.println(route.getPath());
    }
    for (Method method : this.getClass().getMethods()) {
      if(method.isAnnotationPresent(Get.class)){
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
      }else if(method.isAnnotationPresent(Post.class)){
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
      }
    }
    Method[] methods = this.getClass().getDeclaredMethods();
    vertx.createHttpServer().requestHandler(router).listen(6669);
    for (Route route : router.getRoutes()) {
      System.out.println(route.getPath());
    }
  }
}
