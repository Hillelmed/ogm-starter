package io.github.hillelmed.ogm.starter.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@GitModelAnnotation
@Target(ElementType.FIELD)
public @interface GitRepository {
}
