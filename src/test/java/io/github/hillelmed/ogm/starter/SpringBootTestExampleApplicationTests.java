package io.github.hillelmed.ogm.starter;


import io.github.hillelmed.ogm.starter.config.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

@SpringBootTest(classes = {GeneralBeanConfig.class, RepositoryBeanFactory.class})
@ContextConfiguration(classes = GeneralBeanConfig.class)
class SpringBootTestExampleApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    GeneralBeanConfig generalBeanConfig;

    @Autowired
    OgmConfig ogmConfig;

    @Test
    void testContextLoads() {
        Assertions.assertNotNull(applicationContext);
    }

    @Test
    void testOgmConfig() {
        Assertions.assertNotNull(ogmConfig);
        Assertions.assertNotNull(ogmConfig.getCredentials());
        Assertions.assertNotNull(ogmConfig.getUrl());
    }

    @Test
    void testRepoConfig() {
        Assertions.assertNotNull(generalBeanConfig);
        Assertions.assertEquals(generalBeanConfig.repositoryClients().size(), 0);
    }

}
