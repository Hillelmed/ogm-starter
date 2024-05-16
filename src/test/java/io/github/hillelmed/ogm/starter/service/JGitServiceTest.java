package io.github.hillelmed.ogm.starter.service;


import io.github.hillelmed.ogm.starter.config.GeneralBeanConfig;
import io.github.hillelmed.ogm.starter.config.RepositoryBeanFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GeneralBeanConfig.class})
class JGitServiceTest {

    @InjectMocks
    RepositoryBeanFactory repositoryBeanFactory;

    private final String clazzNamePath = "io.github.hillelmed.ogm.starter.model.GenericRepoCrudRepository";
    private final String beanName = "GenericRepoCrudRepository";
    @Mock
    private List<String> listOfRepositoriesClass = Mockito.mock(List.class);
    @Mock
    private ConfigurableBeanFactory beanFactory = Mockito.mock(ConfigurableBeanFactory.class);

    @Test
    void testContextLoads() {
        Assertions.assertNotNull(repositoryBeanFactory);
    }

    @Test
    void testOnPostConstruct() {
//        when(listOfRepositoriesClass.forEach(any())).
        repositoryBeanFactory.loadClassProxyToBean(clazzNamePath);
//        verify(beanFactory, times(1)).registerSingleton(eq(beanName), any());
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


}
