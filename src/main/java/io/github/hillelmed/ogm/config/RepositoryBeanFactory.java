package io.github.hillelmed.ogm.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.exception.*;
import io.github.hillelmed.ogm.framework.*;
import io.github.hillelmed.ogm.repository.GitRepository;
import io.github.hillelmed.ogm.repository.*;
import io.github.hillelmed.ogm.service.*;
import jakarta.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.reflections.*;
import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.*;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RepositoryBeanFactory implements BeanFactoryAware {

    private final List<String> listOfRepository;
    private final OgmConfig ogmConfig;
    @Qualifier("jsonMapper")
    private final ObjectMapper jsonMapper;
    @Qualifier("yamlMapper")
    private final YAMLMapper yamlMapper;
    @Qualifier("xmlMapper")
    private final XmlMapper xmlMapper;
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void onPostConstruct() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;

        try {
            listOfRepository.forEach(name -> {
                Class<?> clazzTypeTGeneric;
                Class<?> clazzToRegistry;
                try {
                    clazzTypeTGeneric = Class.forName(((ParameterizedType) Class.forName(name).getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0].getTypeName());
                    clazzToRegistry = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                try {
                    validateGitRepository(clazzToRegistry);
                    validateGitModel(clazzTypeTGeneric);
                } catch (MissingAnnotationException | UnsupportedOperationException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }

                GitRepositoryImpl gitRepository = new GitRepositoryImpl(ogmConfig,
                        new JGitService(jsonMapper, xmlMapper, yamlMapper),
                        new ReflectionService(),
                        clazzTypeTGeneric);

                String beanName = clazzToRegistry.getName();
                Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), List.of(clazzToRegistry.getNestHost()).toArray(new Class[0]), new DynamicRepositoryInvocationHandler(gitRepository));
                configurableBeanFactory.registerSingleton(beanName, proxyInstance);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void validateGitRepository(Class<?> clazzToRegistry) {
        if (clazzToRegistry.getMethods().length > GitRepository.class.getMethods().length) {
            throw new UnsupportedOperationException("Method are not allowed in repository interface Not supported yet.");
        }
    }

    private void validateGitModel(Class<?> t) throws MissingAnnotationException {
        if (!validateModel(t)) {
            throw new MissingAnnotationException("Some annotation missing in git model");
        }
    }

    private boolean validateModel(Class<?> t) {
        Set<Class<?>> annotations = getGitAnnotationSet();
        if (!t.isAnnotationPresent(GitModel.class)) {
            log.error("Model :" + t.getName() + " missing GitModel annotation");
            return false;
        }
        return 2 <= Arrays.stream(t.getDeclaredFields()).filter(field -> annotations.contains(field.getAnnotations()[0].annotationType())).count();
    }

    private Set<Class<?>> getGitAnnotationSet() {
        Reflections reflections = new Reflections("io.github.hillelmed.ogm.annotation");
        return reflections.getTypesAnnotatedWith(GitModelAnnotation.class);
    }


}
