package io.github.hillelmed.ogm.starter.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

import java.util.*;

import static io.github.hillelmed.ogm.starter.util.OgmAppUtil.*;

/**
 * The type General bean config.
 */
@Configuration
@EnableConfigurationProperties(OgmProperties.class)
@Slf4j
public class GeneralBeanConfig {

    private final OgmProperties properties;
    private final ClassPathScanningCandidateComponentProvider provider;
    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new General bean config.
     *
     * @param properties         the properties
     * @param applicationContext the application context
     */
    public GeneralBeanConfig(OgmProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return super.isCandidateComponent(beanDefinition) || beanDefinition.getMetadata().isAbstract();
            }
        };
        provider.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                if (metadata.getInterfaceNames().length == 0) {
                    return false;
                } else return metadata.getInterfaceNames()[0].equals(GitCrudRepository.class.getCanonicalName());
            }
        });
    }

    /**
     * Json mapper object mapper.
     *
     * @return the object mapper
     */
    @Bean(name = "jsonMapper")
    @ConditionalOnMissingBean
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Xml mapper xml mapper.
     *
     * @return the xml mapper
     */
    @Bean(name = "xmlMapper")
    @ConditionalOnMissingBean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.enable(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper;
    }

    /**
     * Yaml mapper yaml mapper.
     *
     * @return the yaml mapper
     */
    @Bean(name = "yamlMapper")
    @ConditionalOnMissingBean
    public YAMLMapper yamlMapper() {
        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
        return yamlMapper;
    }

    /**
     * Ogm config ogm config.
     *
     * @return the ogm config
     */
    @Bean
    @ConditionalOnMissingBean
    public OgmConfig ogmConfig() {
        OgmConfig config = new OgmConfig();
        config.setCredentials(new UsernamePasswordCredentialsProvider(getUser(), getPass()));
        config.setUrl(getEp());
        return config;
    }


    /**
     * List of repositories class list.
     *
     * @return the list
     */
    @Bean(name = "listOfRepositoriesClass")
    public List<String> listOfRepositoriesClass() {
        try {
            final Set<BeanDefinition> classes = provider.findCandidateComponents(getBasePackagePath());
            List<String> names = new ArrayList<>();
            for (BeanDefinition bean : classes) {
                Class.forName(bean.getBeanClassName());
                names.add(bean.getBeanClassName());
            }
            return names;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getBasePackagePath() {
        Map<String, Object> candidates = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        Class<?> aClass = candidates.isEmpty() ? null : candidates.values().toArray()[0].getClass();
        if (aClass != null) {
            String[] basePackageSplitter = aClass.getName().split("\\.");
            List<String> basePackage = new ArrayList<>(Arrays.stream(basePackageSplitter).toList());
            basePackage.remove(basePackage.size() - 1);
            return String.join(".", basePackage);
        } else {
            return "*";
        }
    }

    private String getEnvOrProp(final String keyEnv, final String keyProp) {
        return System.getenv(keyEnv) == null ? System.getProperty(keyProp) : System.getenv(keyEnv);
    }

    private String getEp() {
        String ep = getEnvOrProp(ENDPOINT_ENV, ENDPOINT_PROP);
        if (ep == null) {
            ep = properties.getUrl();
        } else {
            return ep;
        }
        return ep;
    }

    private String getUser() {
        String user = getEnvOrProp(GIT_USER_ENV, GIT_USER_PROP);
        if (user == null) {
            user = properties.getUsername();
        } else {
            return user;
        }
        return user;
    }

    private String getPass() {
        String pass = getEnvOrProp(GIT_PASSWORD_ENV, GIT_PASSWORD_PROP);
        if (pass == null) {
            pass = properties.getPassword();
        } else {
            return pass;
        }
        return pass;
    }
}
