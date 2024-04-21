package io.github.hillelmed.ogm.starter;

import io.github.hillelmed.ogm.starter.annotation.*;
import io.github.hillelmed.ogm.starter.domain.*;
import lombok.*;

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
