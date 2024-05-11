package io.github.hillelmed.ogm.starter.repository;


import io.github.hillelmed.ogm.starter.annotation.GitFile;
import io.github.hillelmed.ogm.starter.annotation.GitFiles;
import io.github.hillelmed.ogm.starter.config.OgmConfig;
import io.github.hillelmed.ogm.starter.domain.FileType;
import io.github.hillelmed.ogm.starter.domain.GitRepositoryMap;
import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.service.JGitService;
import io.github.hillelmed.ogm.starter.service.ReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UnknownFormatFlagsException;
import java.util.concurrent.atomic.AtomicReference;

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
            throw new OgmRuntimeException(e);
        }
    }


    @Override
    public T update(T t) {
        try {
            return createFileOrFilesUpdateAndPush(t, true);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OgmRuntimeException(e);
        }

    }

    @Override
    public T read(T t) {
        try {
            return getFileOrMapOfFiles(t);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OgmRuntimeException(e);
        }

    }

    @Override
    public T getByRepositoryAndRevision(String repository, String revision) {
        try {
            return getFileOrMapOfFiles(reflectionService.createInstanceAndSetRepoAndRevision(repository, revision));
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            log.error(e.getMessage());
            throw new OgmRuntimeException(e);
        }
    }

    @Override
    public void load(T t) {
        try {
            getFileOrMapOfFiles(t);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OgmRuntimeException(e);
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
            throw new OgmRuntimeException(e);
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
            throw new OgmRuntimeException(e);
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
