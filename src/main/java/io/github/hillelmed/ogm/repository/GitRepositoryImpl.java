package io.github.hillelmed.ogm.repository;


import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.config.*;
import io.github.hillelmed.ogm.domain.*;
import io.github.hillelmed.ogm.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@Slf4j
@RequiredArgsConstructor
public class GitRepositoryImpl<T> implements GitRepository<T> {

    private final OgmConfig ogmConfig;
    private final JGitService jGitService;
    private final ReflectionService<T> reflectionService;

    @Override
    public T sync(T t) {
        try {
            return createFileOrFilesAndSyncDeleteFilesAndPush(t);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public T update(T t) {
        try {
            return createFileOrFilesUpdateAndPush(t, true);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    public T read(T t) {
        try {
            return getFileOrMapOfFiles(t);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    public T getByRepositoryAndRevision(String repository, String revision) {
        try {
            return getFileOrMapOfFiles(reflectionService.createInstanceAndSetRepoAndRevision(repository, revision));
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(T t) {
        try {
            getFileOrMapOfFiles(t);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private T createFileOrFilesAndSyncDeleteFilesAndPush(T t) {
        return createFileOrFilesUpdateAndPush(t, false);
    }

    private T createFileOrFilesUpdateAndPush(T t, boolean isUpdateExistFile) {
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        reflectionService.extractRepositoryAndBranch(t, repo, branch);
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
                    jGitService.writeFileAndPush(ogmConfig, repositoryFieldValue, branchFieldValue, object, gitFileAnnotation, isUpdateExistFile);
                } else {
                    throw new UnknownFormatFlagsException("Missing GitFile annotation");
                }
            } else {
                Field gitFiles = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst().orElse(null);
                if (gitFiles != null) {
                    GitFiles gitFilesAnnotation = ((GitFiles) Arrays.stream(gitFiles.getAnnotations()).filter(GitFiles.class::isInstance).findFirst().orElse(null));
                    gitFiles.setAccessible(true);
                    Class<?> fieldType = gitFiles.getType();
                    GitRepositoryMap gitRepositoryMap = (GitRepositoryMap) fieldType.cast(gitFiles.get(t));
                    if (gitFilesAnnotation != null) {
                        jGitService.syncFilesAndPush(ogmConfig, repositoryFieldValue, branchFieldValue, gitRepositoryMap, isUpdateExistFile);
                    }
                } else {
                    throw new UnknownFormatFlagsException("Missing GitFiles annotation");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;

    }

    private T getFileOrMapOfFiles(T t) {
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        reflectionService.extractRepositoryAndBranch(t, repo, branch);
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
            throw new RuntimeException(e);
        }
        return t;
    }


    private T initGitCloneToMapFilesAnnotations(T t, String repository, String revision, String[] include) throws IllegalAccessException, IOException, GitAPIException {
        final String url = ogmConfig.getUrl() + "/" + repository;
        Git gitInMemoryRepository = jGitService.getGitInMemory(ogmConfig.getCredentials(), url);
        GitRepositoryMap allData = jGitService.loadRemote(gitInMemoryRepository, revision, include);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, allData);
        }
        return t;
    }

    private T initGitCloneToFile(T t, String repository, String revision, FileType fileType, String filePath) throws IllegalAccessException, IOException, GitAPIException {
        final String url = ogmConfig.getUrl() + "/" + repository;
        Git gitInMemoryRepository = jGitService.getGitInMemory(ogmConfig.getCredentials(), url);
        Object res = jGitService.loadRemoteSpesificFile(gitInMemoryRepository, revision, fileType, filePath);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, res);
        }
        return t;
    }

}
