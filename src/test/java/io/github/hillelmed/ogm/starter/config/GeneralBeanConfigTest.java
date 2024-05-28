package io.github.hillelmed.ogm.starter.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class GeneralBeanConfigTest {

    @Mock
    private OgmProperties properties;

    @Mock
    private ApplicationContext applicationContext;

    private GeneralBeanConfig config;

    @BeforeEach
    void setUp() {
        config = new GeneralBeanConfig(properties, applicationContext);
    }

    @Test
    void testJsonMapper() {
        ObjectMapper jsonMapper = config.jsonMapper();
        assertThat(jsonMapper).isNotNull();
        assertThat(jsonMapper.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(jsonMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }

    @Test
    void testXmlMapper() {
        XmlMapper xmlMapper = config.xmlMapper();
        assertThat(xmlMapper).isNotNull();
        assertThat(xmlMapper.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(xmlMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }

    @Test
    void testYamlMapper() {
        YAMLMapper yamlMapper = config.yamlMapper();
        assertThat(yamlMapper).isNotNull();
        assertThat(yamlMapper.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(yamlMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }

    @Test
    void testOgmConfig() {
        when(properties.getUrl()).thenReturn("http://example.com");
        when(properties.getUsername()).thenReturn("user");
        when(properties.getPassword()).thenReturn("pass");

        OgmConfig ogmConfig = config.ogmConfig();

        assertThat(ogmConfig).isNotNull();
        assertThat(ogmConfig.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    void testListOfRepositoriesClass() {
        // Mocking the scanning process and the ApplicationContext
        when(applicationContext.getBeansWithAnnotation(SpringBootApplication.class)).thenReturn(Map.of());

        // This part is tricky to test since it involves classpath scanning.
        // You would need to simulate or mock the provider's behavior
        // This is just a basic example without actual scanning
        List<String> repositoryClasses = config.listOfRepositoriesClass();
        assertThat(repositoryClasses).isNotNull();
    }
}
