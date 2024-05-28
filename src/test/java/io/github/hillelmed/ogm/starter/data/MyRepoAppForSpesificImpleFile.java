package io.github.hillelmed.ogm.starter.data;

import io.github.hillelmed.ogm.starter.repository.GitCrudRepository;

public interface MyRepoAppForSpesificImpleFile extends GitCrudRepository<RepoApplicationFile> {

    void protectTest(String some);
}