package io.github.hillelmed.ogm.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.github.hillelmed.ogm.repository.*;
import lombok.*;
import org.eclipse.jgit.transport.*;
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
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper;
    }

    @Bean
    public ObjectMapper yamlMapper() {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return yamlMapper;
    }

    @Bean
    public AbstractGitRepository abstractGitRepository() {
        return new AbstractGitRepository<>(ogmConfig(),jsonMapper(),xmlMapper(),yamlMapper()) {
        };
    }

    @Bean
    public OgmConfig ogmConfig() {
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
