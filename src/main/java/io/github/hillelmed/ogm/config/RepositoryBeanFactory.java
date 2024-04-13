package io.github.hillelmed.ogm.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.framework.*;
import io.github.hillelmed.ogm.repository.*;
import jakarta.annotation.*;
import lombok.*;
import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.*;

import java.lang.reflect.*;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class RepositoryBeanFactory implements BeanFactoryAware {

    private final List<String> listOfRepository;
    private final OgmConfig ogmConfig;
    @Qualifier("jsonMapper")
    private final ObjectMapper jsonMapper;
    @Qualifier("yamlMapper")
    private final ObjectMapper yamlMapper;
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
                Class<?> clazzTypeTGeneric = null;
                try {
                    clazzTypeTGeneric = Class.forName(((ParameterizedType) Class.forName(name).getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0].getTypeName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Class<?> clazzToRegistry = null;
                try {
                    clazzToRegistry = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                GitRepositoryImpl gitRepository = new GitRepositoryImpl(ogmConfig, jsonMapper, xmlMapper, yamlMapper, clazzTypeTGeneric);

                String beanName = clazzToRegistry.getName();
                Object bean = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), List.of(clazzToRegistry.getNestHost()).toArray(new Class[0]), new DynamicRepositoryInvocationHandler(gitRepository));
                configurableBeanFactory.registerSingleton(beanName, bean);
            });
        } catch (Exception e) {

        }

    }

}
