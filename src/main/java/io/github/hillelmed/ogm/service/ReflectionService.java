package io.github.hillelmed.ogm.service;

import io.github.hillelmed.ogm.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@Slf4j
@RequiredArgsConstructor
public class ReflectionService<T> {

    public void extractRepositoryAndBranch(T t, AtomicReference<Field> fieldRepoAtomic, AtomicReference<Field> fieldBranchAtomic) {
        Field repo = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(io.github.hillelmed.ogm.annotation.GitRepository.class)).findFirst().orElse(null);
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

    public void setRepositoryAndBranch(T t, AtomicReference<Field> repo, AtomicReference<Field> branch, String repository, String revision) throws IllegalAccessException {
        repo.get().set(t, repository);
        branch.get().set(t, revision);
    }
}
