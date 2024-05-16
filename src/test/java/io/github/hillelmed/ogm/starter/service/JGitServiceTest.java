package io.github.hillelmed.ogm.starter.service;


import io.github.hillelmed.ogm.starter.config.GeneralBeanConfig;
import io.github.hillelmed.ogm.starter.config.RepositoryBeanFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest(classes = {GeneralBeanConfig.class})
class JGitServiceTest {

    @InjectMocks
    RepositoryBeanFactory repositoryBeanFactory;

    @Mock
    private List listOfRepositoriesClass = List.of("oneClass.test");
    @Mock
    private BeanFactory beanFactory = Mockito.mock(BeanFactory.class);

    @Test
    void testContextLoads() {
        Assertions.assertNotNull(repositoryBeanFactory);
    }

    @Test
    @DependsOn(value = "testContextLoads")
    void testOnPostConstruct() {
        repositoryBeanFactory.onPostConstruct();
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }



}
