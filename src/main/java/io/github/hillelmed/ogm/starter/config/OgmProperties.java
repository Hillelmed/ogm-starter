package io.github.hillelmed.ogm.starter.config;

import lombok.*;
import org.springframework.boot.context.properties.*;

@Data
@ConfigurationProperties(prefix = "ogm.data.git")
@EnableConfigurationProperties
public class OgmProperties {

    private String url;
    private String username;
    private String password;

}
