package io.github.hillelmed.ogm.dao;


import io.github.hillelmed.ogm.config.*;
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
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGitRepository<T> implements GitRepository<T> {

    private final OgmConfig ogmConfig;

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
        return null;

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
            e.printStackTrace();
        }
        String branchi = null;
        try {
            branchi = (String) branch.get(t);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            initGitCloneToMap(t, reposi, branchi);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initGitCloneToMap(T t, String repository, String revision) throws Exception {
        final String url = ogmConfig.getUrl() + "/" + repository;
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository(ogmConfig.getCredentials(), url);
        Map<String, String> res = loadRemote(inMemoryRepository, url, revision);
        Field f = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().get();
        f.setAccessible(true);
        f.set(t, res);
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
        //treeWalk.setFilter(PathFilter.create(filename));
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


}
