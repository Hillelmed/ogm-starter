package io.github.hillelmed.ogm.starter.service;

import io.github.hillelmed.ogm.starter.annotation.GitRepository;
import io.github.hillelmed.ogm.starter.annotation.GitRevision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Reflection service.
 *
 * @param <T> the type parameter
 */
@Slf4j
@RequiredArgsConstructor
public class ReflectionService<T> {

    private final Class<T> clazzType;

    /**
     * Extract repository and branch.
     *
     * @param t                 the t
     * @param fieldRepoAtomic   the field repo atomic
     * @param fieldBranchAtomic the field branch atomic
     */
    public void extractRepositoryAndBranch(T t, AtomicReference<Field> fieldRepoAtomic, AtomicReference<Field> fieldBranchAtomic) {
        Field repo = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRepository.class)).findFirst().orElse(null);
        Field branch = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRevision.class)).findFirst().orElse(null);
        if (repo == null || branch == null) {
            log.error("Repo or Branch are not according right");
            return;
        }
        repo.setAccessible(true);
        branch.setAccessible(true);
        fieldRepoAtomic.set(repo);
        fieldBranchAtomic.set(branch);
    }

    /**
     * Sets repository and branch.
     *
     * @param t          the t
     * @param repo       the repo
     * @param branch     the branch
     * @param repository the repository
     * @param revision   the revision
     * @throws IllegalAccessException the illegal access exception
     */
    public void setRepositoryAndBranch(T t, AtomicReference<Field> repo, AtomicReference<Field> branch, String repository, String revision) throws IllegalAccessException {
        repo.get().set(t, repository);
        branch.get().set(t, revision);
    }

    /**
     * Create instance and set repo and revision t.
     *
     * @param repository the repository
     * @param revision   the revision
     * @return the t
     * @throws IllegalAccessException    the illegal access exception
     * @throws NoSuchMethodException     the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws InstantiationException    the instantiation exception
     */
    public T createInstanceAndSetRepoAndRevision(String repository, String revision) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        T t = clazzType.getDeclaredConstructor().newInstance();
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        extractRepositoryAndBranch(t, repo, branch);
        setRepositoryAndBranch(t, repo, branch, repository, revision);
        return t;
    }
}
