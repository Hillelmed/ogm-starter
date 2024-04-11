package io.github.hillelmed.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@GitModelAnnotation
@Target(ElementType.FIELD)
public @interface GitFile {

    String[] exclude() default {""};
    String[] include() default {"*"};

}
