package io.github.hillelmed.ogm.starter.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.github.hillelmed.ogm.starter.domain.*;
import lombok.*;

import java.io.*;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OgmAppUtil {

    public static final String ENDPOINT_PROP = "ogm.endpoint";
    public static final String ENDPOINT_ENV = ENDPOINT_PROP.replace("\\.", "_").toUpperCase();

    public static final String GIT_USER_PROP = "ogm.user";
    public static final String GIT_USER_ENV = GIT_USER_PROP.replace("\\.", "_").toUpperCase();

    public static final String GIT_PASSWORD_PROP = "ogm.password";
    public static final String GIT_PASSWORD_ENV = GIT_PASSWORD_PROP.replace("\\.", "_").toUpperCase();

    public static void findTypeAndWriteFile(XmlMapper xmlMapper,
                                            ObjectMapper jsonMapper,
                                            YAMLMapper yamlMapper,
                                            Object content, String path) throws IOException {
        String fileExtension = getFileExtension(path);
        Optional<FileType> fileType = Arrays.stream(FileType.values()).filter(type -> type.name().equals(fileExtension)).findAny();
        if (fileType.isPresent()) {
            writeFileByType(xmlMapper, jsonMapper, yamlMapper, fileType.get(), content, path);
        } else {
            writeFileByType(xmlMapper, jsonMapper, yamlMapper, FileType.TEXT_PLAIN, content, path);
        }
    }

    public static void writeFileByType(XmlMapper xmlMapper,
                                       ObjectMapper jsonMapper,
                                       YAMLMapper yamlMapper,
                                       FileType fileType, Object content, String path) throws IOException {
        // Create the necessary folder structure
        PrintWriter printWriter = getPrintWriter(path);
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
            default -> throw new RuntimeException("Unsupported file type: " + fileType);
        }
        printWriter.close();
    }

    private static PrintWriter getPrintWriter(String path) throws IOException {
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            boolean mkdirResult = parentDir.mkdirs(); // Create parent directories if they don't exist
            if (!mkdirResult) {
                throw new IOException("Unable to create directory " + parentDir);
            }
        }
        FileWriter fileWriter = new FileWriter(path);
        return new PrintWriter(fileWriter);
    }

    public static Object readByType(XmlMapper xmlMapper,
                                    ObjectMapper jsonMapper,
                                    YAMLMapper yamlMapper,
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

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

}
