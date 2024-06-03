package io.github.hillelmed.ogm.starter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.hillelmed.ogm.starter.domain.FileType;
import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;

/**
 * The type Ogm app util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OgmAppUtil {

    /**
     * The constant ENDPOINT_PROP.
     */
    public static final String ENDPOINT_PROP = "ogm.endpoint";
    /**
     * The constant ENDPOINT_ENV.
     */
    public static final String ENDPOINT_ENV = ENDPOINT_PROP.replace('.', '_').toUpperCase();

    /**
     * The constant GIT_USER_PROP.
     */
    public static final String GIT_USER_PROP = "ogm.user";
    /**
     * The constant GIT_USER_ENV.
     */
    public static final String GIT_USER_ENV = GIT_USER_PROP.replace('.', '_').toUpperCase();

    /**
     * The constant GIT_PASSWORD_PROP.
     */
    public static final String GIT_PASSWORD_PROP = "ogm.password";
    /**
     * The constant GIT_PASSWORD_ENV.
     */
    public static final String GIT_PASSWORD_ENV = GIT_PASSWORD_PROP.replace('.', '_').toUpperCase();

    /**
     * Find type and write file.
     *
     * @param xmlMapper  the xml mapper
     * @param jsonMapper the json mapper
     * @param yamlMapper the yaml mapper
     * @param content    the content
     * @param path       the path
     * @throws IOException the io exception
     */
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

    /**
     * Write file by type.
     *
     * @param xmlMapper  the xml mapper
     * @param jsonMapper the json mapper
     * @param yamlMapper the yaml mapper
     * @param fileType   the file type
     * @param content    the content
     * @param path       the path
     * @throws IOException the io exception
     */
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
                    throw new OgmRuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    printWriter.print(jsonMapper.writeValueAsString(content));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new OgmRuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    printWriter.print(yamlMapper.writeValueAsString(content));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new OgmRuntimeException(e);
                }
            }
            default -> throw new OgmRuntimeException("Unsupported file type: " + fileType);
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

    /**
     * Read by type object.
     *
     * @param xmlMapper  the xml mapper
     * @param jsonMapper the json mapper
     * @param yamlMapper the yaml mapper
     * @param fileType   the file type
     * @param string     the string
     * @return the object
     */
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
                    throw new OgmRuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    return jsonMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new OgmRuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    return yamlMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new OgmRuntimeException(e);
                }
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Gets file extension.
     *
     * @param fileName the file name
     * @return the file extension
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

}
