package io.github.hillelmed.ogm.starter.repository;


/**
 * The interface Git crud repository.
 *
 * @param <T> the type parameter
 */
public interface GitCrudRepository<T> {

    /**
     * Sync.
     *
     * @param t the t
     */
    void sync(T t);

    /**
     * Read t.
     *
     * @param t the t
     * @return the t
     */
    T read(T t);

    /**
     * Update.
     *
     * @param t the t
     */
    void update(T t);

    /**
     * Gets by repository and revision.
     *
     * @param repository the repository
     * @param revision   the revision
     * @return the by repository and revision
     */
    T getByRepositoryAndRevision(String repository, String revision);

}
