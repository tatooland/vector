package com.to.core.ann;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Get {
  public String httpMethod() default "get";
  public String path() default "";
}
