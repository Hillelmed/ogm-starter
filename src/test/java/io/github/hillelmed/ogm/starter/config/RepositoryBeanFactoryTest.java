package io.github.hillelmed.ogm.starter.config;


import io.github.hillelmed.ogm.starter.exception.MissingAnnotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = {GeneralBeanConfig.class})
class RepositoryBeanFactoryTest {

    @InjectMocks
    RepositoryBeanFactory repositoryBeanFactory;

    private final String clazzNamePath = "io.github.hillelmed.ogm.starter.config.model.GenericRepoCrudRepository";
    private final String clazzNamePathWithMissingAnnotations = "io.github.hillelmed.ogm.starter.config.model.MyRepoAppForSpesificFile";
    private final String clazzNamePathWithImplNotAllowed = "io.github.hillelmed.ogm.starter.config.model.MyRepoAppForSpesificImpleFile";
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
        Assertions.assertThrows(MissingAnnotation.class, () -> repositoryBeanFactory.loadClassProxyToBean(clazzNamePathWithMissingAnnotations));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> repositoryBeanFactory.loadClassProxyToBean(clazzNamePathWithImplNotAllowed));
    //TODO change to other function for each type of error
//        verify(beanFactory, times(1)).registerSingleton(eq(beanName), any());
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


}
