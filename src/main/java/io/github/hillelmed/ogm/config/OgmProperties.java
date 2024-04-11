package io.github.hillelmed.ogm.config;

import lombok.*;
import org.springframework.boot.context.properties.*;

@Data
@ConfigurationProperties(prefix = "spring.data.git")
public class OgmProperties {

    private String url;
    private String username;
    private String password;

}
