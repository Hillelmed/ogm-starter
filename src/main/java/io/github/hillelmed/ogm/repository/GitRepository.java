package io.github.hillelmed.ogm.repository;


public interface GitRepository<T> {

    T sync(T t);

    T update(T t);

    T getByRepositoryAndRevision(String repository, String revision);

    T read(T t);

    void load(T t);

}
