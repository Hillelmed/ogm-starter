package io.github.hillelmed.ogm.starter.domain;

/**
 * The enum File type.
 */
public enum FileType {


    /**
     * The Json.
     */
    JSON(new String[]{".json"}),
    /**
     * The Xml.
     */
    XML(new String[]{".xml"}),
    /**
     * The Yaml.
     */
    YAML(new String[]{".yml", ".yaml"}),
    /**
     * The Text plain.
     */
    TEXT_PLAIN(new String[]{});

    /**
     * The Suffix.
     */
    final String[] suffix;

    FileType(String[] suffix) {
        this.suffix = suffix;
    }
}
