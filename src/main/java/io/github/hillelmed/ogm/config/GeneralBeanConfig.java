package io.github.hillelmed.ogm.config;

import io.github.hillelmed.ogm.dao.*;
import jakarta.annotation.*;
import lombok.*;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.hillelmed.ogm.util.OgmAppUtil.*;

@Configuration
@EnableConfigurationProperties(OgmProperties.class)
@RequiredArgsConstructor
public class GeneralBeanConfig {

    private final OgmProperties properties;

    @Bean
    public AbstractGitRepository abstractGitRepository() {
        return new AbstractGitRepository<>(config()) {
        };
    }

    @Bean
    public OgmConfig config() {
        OgmConfig config = new OgmConfig();
        config.setCredentials(new UsernamePasswordCredentialsProvider(getUser(), getPass()));
        config.setUrl(getEp());
        return config;
    }


    private String getEnvOrProp(final String keyEnv, final String keyProp) {
        return System.getenv(keyEnv) == null ? System.getProperty(keyProp) : System.getenv(keyEnv);
    }

    private String getEp() {
        String ep;
        if (getEnvOrProp(ENDPOINT_ENV, ENDPOINT_PROP) == null) {
            ep = properties.getUrl();
        } else {
            ep = getEnvOrProp(ENDPOINT_ENV, ENDPOINT_PROP);
        }
        if (ep == null) {
            ep = DEFAULT_ENDPOINT;
        }
        return ep;
    }

    private String getUser() {
        String user;
        if (getEnvOrProp(GIT_USER_ENV, GIT_USER_PROP) == null) {
            user = properties.getUsername();
        } else {
            user = getEnvOrProp(GIT_USER_ENV, GIT_USER_PROP);
        }
        if (user == null) {
            user = GIT_USER;
        }
        return user;
    }

    private String getPass() {
        String pass;
        if (getEnvOrProp(GIT_PASSWORD_ENV, GIT_PASSWORD_PROP) == null) {
            pass = properties.getPassword();
        } else {
            pass = getEnvOrProp(GIT_PASSWORD_ENV, GIT_PASSWORD_PROP);
        }
        if (pass == null) {
            pass = GIT_PASSWORD;
        }
        return pass;
    }
}
