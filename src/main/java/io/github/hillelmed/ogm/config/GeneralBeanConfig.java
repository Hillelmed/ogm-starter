package io.github.hillelmed.ogm.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.config.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.context.support.*;
import org.springframework.core.type.filter.*;
import org.springframework.stereotype.*;

import java.util.*;

import static io.github.hillelmed.ogm.util.OgmAppUtil.*;

@Configuration
@EnableConfigurationProperties(OgmProperties.class)
public class GeneralBeanConfig {

    private final OgmProperties properties;
    private final GenericApplicationContext genericApplicationContext;
    private final ClassPathScanningCandidateComponentProvider provider;

    public GeneralBeanConfig(OgmProperties properties, GenericApplicationContext genericApplicationContext) {
        this.properties = properties;
        this.genericApplicationContext = genericApplicationContext;
        provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return super.isCandidateComponent(beanDefinition) || beanDefinition.getMetadata().isAbstract();
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(Repository.class, true, true));
    }

    @Bean(name = "jsonMapper")
    @ConditionalOnMissingBean
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean(name = "xmlMapper")
    @ConditionalOnMissingBean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper;
    }

    @Bean(name = "yamlMapper")
    public ObjectMapper yamlMapper() {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return yamlMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public OgmConfig ogmConfig() {
        OgmConfig config = new OgmConfig();
        config.setCredentials(new UsernamePasswordCredentialsProvider(getUser(), getPass()));
        config.setUrl(getEp());
        return config;
    }


    @Bean(name = "listOfRepository")
    public List<String> repositoryClients() {
        final Set<BeanDefinition> classes = provider.findCandidateComponents("*");
        List<String> names = new ArrayList<>();
        for (BeanDefinition bean : classes) {
            try {
                Class.forName(bean.getBeanClassName());
                names.add(bean.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return names;
    }

//    @Bean
//    public <T> T getProxy() throws ClassNotFoundException {
//        List<String> client = repositoryClients();
//        String name = client.get(0);
//
//        Class<?> clazzTypeTGeneric = Class.forName(((ParameterizedType) Class.forName(name).getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0].getTypeName());
//        Class<?> clazzToRegistry = Class.forName(name);
//
//        GitRepositoryImpl bean = new GitRepositoryImpl(ogmConfig(), jsonMapper(), xmlMapper(), yamlMapper(), clazzTypeTGeneric);
//
//        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), List.of(clazzToRegistry.getNestHost()).toArray(new Class[0]), new DynamicRepositoryInvocationHandler(bean));
//    }


//    public void registerBeanDefinitions(List<String> names) {
//        names.forEach(
//                this::register
//        );
//    }
//
//
//    private void register(String name) {
//        try {
//            if (((ParameterizedType) Class.forName(name).getAnnotatedInterfaces()[0].getType()).getRawType().getTypeName().equals(GitRepository.class.getName())) {
//                Class<?> clazzTypeTGeneric = Class.forName(((ParameterizedType) Class.forName(name).getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0].getTypeName());
//                Class<?> clazzToRegistry = Class.forName(name);
//
////                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AbstractGitRepository.class);
////                builder.addPropertyValue("ogmConfig", ogmConfig());
////                builder.addPropertyValue("objectMapper", objectMapper());
//////                builder.addPropertyValue("xmlMapper", xmlMapper());
//////                builder.addPropertyValue("yamlMapper", yamlMapper());
////                builder.addPropertyValue("clazzType", clazzTypeTGeneric);
////                ProxyFactory factory = new ProxyFactory(new SimplePojo());
////                factory.addInterface(Pojo.class);
////                factory.addAdvice(new RetryAdvice());
////                factory.setExposeProxy(true);
////                GitRepository proxyInstance = (GitRepository) Proxy.newProxyInstance(
////                        OgmConfig.class.getClassLoader(),
////                        new Class[] { GitRepository.class },
////                        new DynamicRepositoryInvocationHandler());
//
////                GitRepositoryImpl bean = new GitRepositoryImpl();
////                InvocationHandler handler = new DynamicRepositoryInvocationHandler(bean);
////                Object proxyObj = Proxy.newProxyInstance(OgmConfig.class.getClassLoader(), clazzToRegistry.getInterfaces(), handler);
//
////                genericApplicationContext.registerBean(Object.class, new Object());
////                genericApplicationContext.registerBeanDefinition(clazzToRegistry.getName(), builder.getBeanDefinition());
//            }
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
