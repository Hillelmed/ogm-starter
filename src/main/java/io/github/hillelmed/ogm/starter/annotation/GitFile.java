package io.github.hillelmed.ogm.starter.annotation;

import io.github.hillelmed.ogm.starter.domain.FileType;

import java.lang.annotation.*;

/**
 * The interface Git file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface GitFile {

    /**
     * Path string.
     *
     * @return the string
     */
    String path() default "";

    /**
     * Type file type.
     *
     * @return the file type
     */
    FileType type() default FileType.TEXT_PLAIN;
}
