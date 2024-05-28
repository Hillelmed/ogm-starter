package io.github.hillelmed.ogm.starter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileTypeTest {

    @Test
    void testJsonSuffix() {
        FileType fileType = FileType.JSON;
        assertArrayEquals(new String[]{".json"}, fileType.suffix);
    }

    @Test
    void testXmlSuffix() {
        FileType fileType = FileType.XML;
        assertArrayEquals(new String[]{".xml"}, fileType.suffix);
    }

    @Test
    void testYamlSuffix() {
        FileType fileType = FileType.YAML;
        assertArrayEquals(new String[]{".yml", ".yaml"}, fileType.suffix);
    }

    @Test
    void testTextPlainSuffix() {
        FileType fileType = FileType.TEXT_PLAIN;
        assertArrayEquals(new String[]{}, fileType.suffix);
    }

    @Test
    void testFileTypeValues() {
        FileType[] fileTypes = FileType.values();
        assertEquals(4, fileTypes.length);
        assertEquals(FileType.JSON, fileTypes[0]);
        assertEquals(FileType.XML, fileTypes[1]);
        assertEquals(FileType.YAML, fileTypes[2]);
        assertEquals(FileType.TEXT_PLAIN, fileTypes[3]);
    }

    @Test
    void testFileTypeValueOf() {
        assertEquals(FileType.JSON, FileType.valueOf("JSON"));
        assertEquals(FileType.XML, FileType.valueOf("XML"));
        assertEquals(FileType.YAML, FileType.valueOf("YAML"));
        assertEquals(FileType.TEXT_PLAIN, FileType.valueOf("TEXT_PLAIN"));
    }
}
