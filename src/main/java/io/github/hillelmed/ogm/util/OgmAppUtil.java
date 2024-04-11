package io.github.hillelmed.ogm.util;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OgmAppUtil {

    public static final String DEFAULT_ENDPOINT = "http://127.0.0.1:7990";
    public static final String GIT_USER = "admin";
    public static final String GIT_PASSWORD = "admin";


    public static final String ENDPOINT_PROP = "ogm.endpoint";
    public static final String ENDPOINT_ENV = ENDPOINT_PROP.replaceAll("\\.", "_").toUpperCase();

    public static final String GIT_USER_PROP = "ogm.user";
    public static final String GIT_USER_ENV = GIT_USER_PROP.replaceAll("\\.", "_").toUpperCase();

    public static final String GIT_PASSWORD_PROP = "ogm.password";
    public static final String GIT_PASSWORD_ENV = GIT_PASSWORD_PROP.replaceAll("\\.", "_").toUpperCase();

}
