package io.github.hillelmed.ogm.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.hillelmed.ogm.starter.annotation.*;
import io.github.hillelmed.ogm.starter.exception.MissingAnnotation;
import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.invocation.DynamicRepositoryInvocationHandler;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepository;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepositoryImpl;
import io.github.hillelmed.ogm.starter.service.JGitService;
import io.github.hillelmed.ogm.starter.service.ReflectionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RepositoryBeanFactory implements BeanFactoryAware {

    private final OgmConfig ogmConfig;
    @Qualifier("listOfRepositoriesClass")
    private final List<String> listOfRepositoriesClass;
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
        try {
            listOfRepositoriesClass.forEach(this::loadClassProxyToBean);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void loadClassProxyToBean(String clazzName) {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        Class<?> clazzTypeTGeneric;
        Class<?> clazzToRegistry;
        try {
            clazzTypeTGeneric = Class.forName(((ParameterizedType) Class.forName(clazzName).getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0].getTypeName());
            clazzToRegistry = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            throw new OgmRuntimeException(e);
        }
        try {
            validateGitRepository(clazzToRegistry);
            validateGitModel(clazzTypeTGeneric);
        } catch (MissingAnnotation | UnsupportedOperationException e) {
            log.error(e.getMessage());
            throw e;
        }

        GitCrudRepositoryImpl gitRepository = new GitCrudRepositoryImpl(ogmConfig,
                new JGitService(jsonMapper, xmlMapper, yamlMapper),
                new ReflectionService(clazzTypeTGeneric));

        String beanName = clazzToRegistry.getName();
        Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                List.of(clazzToRegistry.getNestHost()).toArray(new Class[0]),
                new DynamicRepositoryInvocationHandler(gitRepository));
        configurableBeanFactory.registerSingleton(beanName, proxyInstance);
    }

    private void validateGitRepository(Class<?> clazzToRegistry) {
        if (clazzToRegistry.getMethods().length > GitCrudRepository.class.getMethods().length) {
            throw new UnsupportedOperationException("Method are not allowed in repository interface Not supported yet.");
        }
    }

    private void validateGitModel(Class<?> t) throws MissingAnnotation {
        if (!validateModel(t)) {
            throw new MissingAnnotation("Some annotation missing in git model");
        }
    }

    private boolean validateModel(Class<?> t) {
        if (!t.isAnnotationPresent(GitModel.class)) {
            log.error("Model :{} missing GitModel annotation", t.getName());
            return false;
        }
        return validateEachAnnotationsExist(t.getDeclaredFields(), t);
    }

    private boolean validateEachAnnotationsExist(Field[] declaredFields, Class<?> t) {
        if (declaredFields.length == 0) {
            log.error("Model :{} missing GitRepository,GitRevision And GitFile Or GitFiles annotation", t.getName());
            return false;
        }
        if (Arrays.stream(declaredFields).noneMatch(field -> field.isAnnotationPresent(GitRepository.class))) {
            log.error("Model :{} missing GitRepository annotation", t.getName());
            return false;
        }
        if (Arrays.stream(declaredFields).noneMatch(field -> field.isAnnotationPresent(GitRevision.class))) {
            log.error("Model :{} missing GitRevision annotation", t.getName());
            return false;
        }
        if (Arrays.stream(declaredFields).noneMatch(field -> field.isAnnotationPresent(GitFile.class))
                && Arrays.stream(declaredFields).noneMatch(field -> field.isAnnotationPresent(GitFiles.class))) {
            log.error("Model :{} missing GitRevision annotation", t.getName());
            return false;
        }
        return true;
    }

}
