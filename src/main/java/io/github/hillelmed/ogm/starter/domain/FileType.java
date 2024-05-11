package io.github.hillelmed.ogm.starter.domain;

public enum FileType {


    JSON(new String[]{".json"}),
    XML(new String[]{".xml"}),
    YAML(new String[]{".yml", ".yaml"}),
    TEXT_PLAIN(new String[]{});

    final String[] suffix;

    FileType(String[] suffix) {
        this.suffix = suffix;
    }
}
