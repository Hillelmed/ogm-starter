package io.github.hillelmed.ogm.starter.repository;


public interface GitCrudRepository<T> {

    void sync(T t);

    void update(T t);

    T getByRepositoryAndRevision(String repository, String revision);

    T read(T t);

    void load(T t);

}
