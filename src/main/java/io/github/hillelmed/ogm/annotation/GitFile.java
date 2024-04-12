package io.github.hillelmed.ogm.annotation;

import io.github.hillelmed.ogm.domain.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@GitModelAnnotation
@Target(ElementType.FIELD)
public @interface GitFile {

    String path() default "";

    FileType type() default FileType.TEXT_PLAIN;
}