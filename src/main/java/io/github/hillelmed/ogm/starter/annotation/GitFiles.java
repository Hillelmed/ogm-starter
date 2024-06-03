package io.github.hillelmed.ogm.starter.annotation;

import java.lang.annotation.*;

/**
 * The interface Git files.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface GitFiles {

    /**
     * Include string [ ].
     *
     * @return the string [ ]
     */
    String[] include() default {};
}
