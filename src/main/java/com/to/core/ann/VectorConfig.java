package com.to.core.ann;



import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VectorConfig {
  public boolean shareMySqlPool() default false;
  public boolean independentMySqlPool() default false;
  public boolean shareRedisPool() default false;

  public boolean independentRedisPool() default false;
  public boolean allowCORS() default false;
  public int port() default -1;
}
