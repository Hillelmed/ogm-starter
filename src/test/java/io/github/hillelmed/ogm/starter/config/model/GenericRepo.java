package io.github.hillelmed.ogm.starter.config.model;


import io.github.hillelmed.ogm.starter.annotation.GitFiles;
import io.github.hillelmed.ogm.starter.annotation.GitModel;
import io.github.hillelmed.ogm.starter.annotation.GitRepository;
import io.github.hillelmed.ogm.starter.annotation.GitRevision;
import io.github.hillelmed.ogm.starter.domain.GitRepositoryMap;
import lombok.Data;

@GitModel
@Data
public class GenericRepo {

    @GitRepository
    private String repo;

    @GitRevision
    private String branch;

    @GitFiles
    private GitRepositoryMap files;

}
