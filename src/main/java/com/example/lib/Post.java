package com.example.lib;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Post {
  public String httpMethod() default "post";
  public String path() default "";
}
