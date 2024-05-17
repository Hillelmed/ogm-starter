package io.github.hillelmed.ogm.starter.repository;


public interface GitCrudRepository<T> {

    void sync(T t);

    T read(T t);

    void update(T t);

    T getByRepositoryAndRevision(String repository, String revision);

}
