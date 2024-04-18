package io.github.hillelmed.ogm.starter.annotation;

import io.github.hillelmed.ogm.starter.domain.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@GitModelAnnotation
@Target(ElementType.FIELD)
public @interface GitFile {

    String path() default "";

    FileType type() default FileType.TEXT_PLAIN;
}
