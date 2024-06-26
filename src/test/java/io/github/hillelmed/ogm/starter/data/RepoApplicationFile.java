package io.github.hillelmed.ogm.starter.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.hillelmed.ogm.starter.annotation.GitFile;
import io.github.hillelmed.ogm.starter.annotation.GitModel;
import io.github.hillelmed.ogm.starter.annotation.GitRepository;
import io.github.hillelmed.ogm.starter.domain.FileType;
import lombok.Data;

@GitModel
@Data
public class RepoApplicationFile {

    @GitRepository
    private String repo;

    private String branch;

    @GitFile(path = "not-exist-file/application.yml", type = FileType.YAML)
    private JsonNode applicationYaml;

}
