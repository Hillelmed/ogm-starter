package io.github.hillelmed.ogm.repository;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.config.*;
import io.github.hillelmed.ogm.domain.*;
import io.github.hillelmed.ogm.exception.*;
import io.github.hillelmed.ogm.util.*;
import jakarta.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.dfs.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.*;
import org.reflections.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
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


    @PostConstruct
    public void init() throws MissingAnnotationException {
        if (!validateModel(clazzType)) {
            throw new MissingAnnotationException("Some annotation missing in git model");
        }
    }

    protected boolean validateModel(Class<T> t) {
        Set<Class<?>> annotations = getGitAnnotationSet();
        if (!t.isAnnotationPresent(GitModel.class)) {
            log.error("Model :" + t.getName() + " missing GitModel annotation");
            return false;
        }
        return 2 <= Arrays.stream(t.getDeclaredFields()).filter(field -> annotations.contains(field.getAnnotations()[0].annotationType())).count();
    }

    private Set<Class<?>> getGitAnnotationSet() {
        Reflections reflections = new Reflections("io.github.hillelmed.ogm.annotation");
        return reflections.getTypesAnnotatedWith(GitModelAnnotation.class);
    }

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public T create(T t) {
        return null;

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


    private void setRepositoryAndBranch(T t) {
        setRepositoryAndBranch(t, null, null);
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

    private T getFileOrMapOfFiles(T t) {
        AtomicReference<Field> repo = new AtomicReference<>();
        AtomicReference<Field> branch = new AtomicReference<>();
        setRepositoryAndBranch(t, repo, branch);
        String reposi = null;
        try {
            reposi = (String) repo.get().get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        String branchi = null;
        try {
            branchi = (String) branch.get().get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        try {
            Field gitFile = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().orElse(null);
            if (gitFile != null) {
                GitFile gitFileAnnotation = ((GitFile) Arrays.stream(gitFile.getAnnotations()).filter(GitFile.class::isInstance).findFirst().orElse(null));
                if (gitFileAnnotation != null) {
                    return initGitCloneToFile(t, reposi, branchi, gitFileAnnotation.type(), gitFileAnnotation.path());
                }
            } else {
                Field gitFiles = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst().orElse(null);
                if (gitFiles != null) {
                    GitFiles gitFilesAnnotation = ((GitFiles) Arrays.stream(gitFiles.getAnnotations()).filter(GitFiles.class::isInstance).findFirst().orElse(null));
                    if (gitFilesAnnotation != null) {
                        return initGitCloneToMapFilesAnnotations(t, reposi, branchi, gitFilesAnnotation.include());
                    }
                }
            }
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        return t;
    }

    private T initGitCloneToMapFilesAnnotations(T t, String repository, String revision, String[] include) throws Exception {
        final String url = ogmConfig.getUrl() + "/" + repository;
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository(ogmConfig.getCredentials(), url);
        Map<String, String> res = loadRemote(inMemoryRepository, url, revision, include);
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
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository(ogmConfig.getCredentials(), url);
        Object res = loadRemoteSpesificFile(inMemoryRepository, url, revision, fileType, filePath);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, res);
        }
        return t;
    }

    private Map<String, String> loadRemote(InMemoryRepository repo, String url, String revision, String[] include) throws Exception {
        Git git = new Git(repo);
        git.fetch().setRemote(url).setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*")).call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + revision);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        if (include != null && include.length > 0) {
            Collection<PathFilter> pathFilters = Arrays.stream(include).map(PathFilter::create).toList();
            treeWalk.setFilter(PathFilterGroup.create(pathFilters));
        }
        Map<String, String> files = new HashMap<>();
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            loader.copyTo(byteArrayOutputStream);
            files.put(path, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        }
        return files;
    }

    private Object loadRemoteSpesificFile(InMemoryRepository repo, String url, String revision, FileType fileType, String pathFile) throws Exception {
        Git git = new Git(repo);
        git.fetch().setRemote(url).setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*")).call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + revision);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(pathFile));
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            if (path.equals(pathFile)) {
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repo.open(objectId);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                loader.copyTo(byteArrayOutputStream);
                return readByType(fileType, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
            }
        }
        return null;
    }

    private Object readByType(FileType fileType, String string) {
        switch (fileType) {
            case TEXT_PLAIN -> {
                return string;
            }
            case XML -> {
                try {
                    return xmlMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    return jsonMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    return yamlMapper.readValue(string, JsonNode.class);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                return null;
            }
        }
    }


}
