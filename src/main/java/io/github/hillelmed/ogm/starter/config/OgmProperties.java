package io.github.hillelmed.ogm.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * The type Ogm properties.
 */
@Data
@ConfigurationProperties(prefix = "ogm.data.git")
@EnableConfigurationProperties
public class OgmProperties {

    private String url = "https://api.github.com";
    private String username = "admin";
    private String password = "admin";

}
