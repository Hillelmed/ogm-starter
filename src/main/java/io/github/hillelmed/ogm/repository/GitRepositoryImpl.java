package io.github.hillelmed.ogm.repository;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.config.*;
import io.github.hillelmed.ogm.domain.*;
import io.github.hillelmed.ogm.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.dfs.*;
import org.springframework.beans.factory.annotation.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@Slf4j
@RequiredArgsConstructor
public class GitRepositoryImpl<T> implements GitRepository<T> {

    private final OgmConfig ogmConfig;
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    @Qualifier("yamlMapper")
    private final ObjectMapper yamlMapper;
    private final Class<T> clazzType;

    @Override
    public T getByRepositoryAndRevision(String repository, String revision) {
        try {
            T t = clazzType.getDeclaredConstructor().newInstance();
            AtomicReference<Field> repo = new AtomicReference<>();
            AtomicReference<Field> branch = new AtomicReference<>();
            setRepositoryAndBranch(t, repo, branch);
            repo.get().set(t, repository);
            branch.get().set(t, revision);
            return getFileOrMapOfFiles(t);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public T create(T t) {
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        setRepositoryAndBranch(t, repo, branch);
        try {
            String repositoryFieldValue = (String) repo.get().get(t);
            String branchFieldValue = (String) branch.get().get(t);
            Field gitFile = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().orElse(null);
            if (gitFile != null) {
                GitFile gitFileAnnotation = ((GitFile) Arrays.stream(gitFile.getAnnotations()).filter(GitFile.class::isInstance).findFirst().orElse(null));
                if (gitFileAnnotation != null) {
                    gitFile.setAccessible(true);
                    Class<?> fieldType = gitFile.getType();
                    Object object = fieldType.cast(gitFile.get(t));
                    JGitUtil.writeFileAndPush(ogmConfig, xmlMapper, jsonMapper, yamlMapper, repositoryFieldValue, branchFieldValue, object, gitFileAnnotation);
                } else {
                    throw new FileNotFoundException(repositoryFieldValue);
                }
            } else {
                Field gitFiles = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst().orElse(null);
                if (gitFiles != null) {
                    GitFiles gitFilesAnnotation = ((GitFiles) Arrays.stream(gitFiles.getAnnotations()).filter(GitFiles.class::isInstance).findFirst().orElse(null));
                    if (gitFilesAnnotation != null) {
                        throw new UnsupportedOperationException("Not support yes");
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return t;

    }


    @Override
    public T update(T t) {
        return null;
    }

    @Override
    public T read(T t) {
        return getFileOrMapOfFiles(t);
    }

    @Override
    public void load(T t) {
        getFileOrMapOfFiles(t);
    }

    private T getFileOrMapOfFiles(T t) {
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        setRepositoryAndBranch(t, repo, branch);
        try {
            String repositoryFieldValue = (String) repo.get().get(t);
            String branchFieldValue = (String) branch.get().get(t);
            Field gitFile = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().orElse(null);
            if (gitFile != null) {
                GitFile gitFileAnnotation = ((GitFile) Arrays.stream(gitFile.getAnnotations()).filter(GitFile.class::isInstance).findFirst().orElse(null));
                if (gitFileAnnotation != null) {
                    return initGitCloneToFile(t, repositoryFieldValue, branchFieldValue, gitFileAnnotation.type(), gitFileAnnotation.path());
                }
            } else {
                Field gitFiles = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst().orElse(null);
                if (gitFiles != null) {
                    GitFiles gitFilesAnnotation = ((GitFiles) Arrays.stream(gitFiles.getAnnotations()).filter(GitFiles.class::isInstance).findFirst().orElse(null));
                    if (gitFilesAnnotation != null) {
                        return initGitCloneToMapFilesAnnotations(t, repositoryFieldValue, branchFieldValue, gitFilesAnnotation.include());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return t;
    }

    private void setRepositoryAndBranch(T t, AtomicReference<Field> fieldRepoAtomic, AtomicReference<Field> fieldBranchAtomic) {
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

    private T initGitCloneToMapFilesAnnotations(T t, String repository, String revision, String[] include) throws Exception {
        final String url = ogmConfig.getUrl() + "/" + repository;
        Git gitInMemoryRepository = JGitUtil.getGitInMemory(ogmConfig.getCredentials(), url);
        Map<String, String> res = JGitUtil.loadRemote(gitInMemoryRepository, revision, include);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, res);
        }
        return t;
    }

    private T initGitCloneToFile(T t, String repository, String revision, FileType fileType, String filePath) throws Exception {
        final String url = ogmConfig.getUrl() + "/" + repository;
        Git gitInMemoryRepository = JGitUtil.getGitInMemory(ogmConfig.getCredentials(), url);
        Object res = JGitUtil.loadRemoteSpesificFile(xmlMapper, jsonMapper, yamlMapper, gitInMemoryRepository, revision, fileType, filePath);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, res);
        }
        return t;
    }

}
