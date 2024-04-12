package io.github.hillelmed.ogm.repository;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.config.*;
import io.github.hillelmed.ogm.domain.*;
import io.github.hillelmed.ogm.git.util.JGitUtil;
import io.github.hillelmed.ogm.annotation.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGitRepository<T> implements GitRepository<T> {

    private final OgmConfig ogmConfig;
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    private final ObjectMapper yamlMapper;

    @Override
    public T getByRepositoryAndRevision(String repository, String revision) {
        return null;
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
        Field repo = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(io.github.hillelmed.ogm.annotation.GitRepository.class)).findFirst().get();
        Field branch = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRevision.class)).findFirst().get();
        repo.setAccessible(true);
        branch.setAccessible(true);
        String reposi = null;
        try {
            reposi = (String) repo.get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        String branchi = null;
        try {
            branchi = (String) branch.get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        Field gitFile = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().get();
        FileType gitFileType = ((GitFile) Arrays.stream(gitFile.getAnnotations()).filter(GitFile.class::isInstance).findFirst().get()).type();
        String gitFilePath = ((GitFile) Arrays.stream(gitFile.getAnnotations()).filter(GitFile.class::isInstance).findFirst().get()).path();
        try {
            return initGitCloneToFile(t, reposi, branchi, gitFileType, gitFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(T t) {
        Field repo = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(io.github.hillelmed.ogm.annotation.GitRepository.class)).findFirst().get();
        Field branch = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRevision.class)).findFirst().get();
        repo.setAccessible(true);
        branch.setAccessible(true);
        String reposi = null;
        try {
            reposi = (String) repo.get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        String branchi = null;
        try {
            branchi = (String) branch.get(t);
        } catch (IllegalAccessException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        try {
            initGitCloneToMapFilesAnnotations(t, reposi, branchi);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initGitCloneToMapFilesAnnotations(T t, String repository, String revision) throws Exception {
        final String url = ogmConfig.getUrl() + "/" + repository;
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository(ogmConfig.getCredentials(), url);
        Map<String, String> res = loadRemote(inMemoryRepository, url, revision);
        Optional<Field> fieldOptional = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFiles.class)).findFirst();
        if (fieldOptional.isPresent()) {
            Field f = fieldOptional.get();
            f.setAccessible(true);
            f.set(t, res);
        }
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

    private Map<String, String> loadRemote(InMemoryRepository repo, String url, String revision) throws Exception {
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
                    xmlMapper.readValue(string, JsonNode.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case JSON -> {
                try {
                    return jsonMapper.readValue(string, JsonNode.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            case YAML -> {
                try {
                    return yamlMapper.readValue(string, JsonNode.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                return null;
            }
        }
        return null;
    }


}
