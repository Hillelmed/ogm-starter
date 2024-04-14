package io.github.hillelmed.ogm.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.domain.*;
import lombok.*;

import java.io.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OgmAppUtil {

    public static final String DEFAULT_ENDPOINT = "http://127.0.0.1:7990";
    public static final String GIT_USER = "admin";
    public static final String GIT_PASSWORD = "admin";


    public static final String ENDPOINT_PROP = "ogm.endpoint";
    public static final String ENDPOINT_ENV = ENDPOINT_PROP.replaceAll("\\.", "_").toUpperCase();

    public static final String GIT_USER_PROP = "ogm.user";
    public static final String GIT_USER_ENV = GIT_USER_PROP.replaceAll("\\.", "_").toUpperCase();

    public static final String GIT_PASSWORD_PROP = "ogm.password";
    public static final String GIT_PASSWORD_ENV = GIT_PASSWORD_PROP.replaceAll("\\.", "_").toUpperCase();

    public static void writeFileByType(XmlMapper xmlMapper,
                                       ObjectMapper jsonMapper,
                                       ObjectMapper yamlMapper,
                                       FileType fileType, Object content, String path) throws IOException {
        FileWriter fileWriter = new FileWriter(path);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        switch (fileType) {
            case TEXT_PLAIN -> printWriter.print(content);
            case XML -> {
                try {
                    printWriter.print(xmlMapper.writeValueAsString(content));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    printWriter.print(jsonMapper.writeValueAsString(content));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    printWriter.print(yamlMapper.writeValueAsString(content));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                throw new RuntimeException("Unsupported file type: " + fileType);
            }
        }
        printWriter.close();
    }

    public static Object readByType(XmlMapper xmlMapper,
                                    ObjectMapper jsonMapper,
                                    ObjectMapper yamlMapper,
                                    FileType fileType, String string) {
        switch (fileType) {
            case TEXT_PLAIN -> {
                return string;
            }
            case XML -> {
                try {
                    return xmlMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    return jsonMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    return yamlMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                return null;
            }
        }
    }

}
