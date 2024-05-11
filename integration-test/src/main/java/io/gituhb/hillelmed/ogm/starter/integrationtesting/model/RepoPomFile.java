package io.gituhb.hillelmed.ogm.starter.integrationtesting.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.hillelmed.ogm.starter.annotation.GitFile;
import io.github.hillelmed.ogm.starter.annotation.GitModel;
import io.github.hillelmed.ogm.starter.annotation.GitRepository;
import io.github.hillelmed.ogm.starter.annotation.GitRevision;
import io.github.hillelmed.ogm.starter.domain.FileType;
import lombok.Data;

@GitModel
@Data
public class RepoPomFile {

    @GitRepository
    private String repo;

    @GitRevision
    private String branch;

    @GitFile(path = "pom.xml", type = FileType.XML)
    private JsonNode pomXml;

}
