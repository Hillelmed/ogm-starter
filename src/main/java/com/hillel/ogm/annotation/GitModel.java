package com.hillel.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@GitModelAnnotation
@Target(ElementType.TYPE)
public @interface GitModel {

}
