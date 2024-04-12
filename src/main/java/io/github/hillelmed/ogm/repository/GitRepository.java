package io.github.hillelmed.ogm.repository;


public interface GitRepository<T> {

    T getByRepositoryAndRevision(Class<T> t, String repository, String revision);

    T create(T t);

    T update(T t);

    T read(T t);

    void load(T t);

}
