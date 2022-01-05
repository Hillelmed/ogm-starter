package com.hillel.ogm.git.impl;

import com.hillel.ogm.annotation.GitFile;
import com.hillel.ogm.annotation.GitRepository;
import com.hillel.ogm.annotation.GitRevision;
import com.hillel.ogm.git.GitManager;
import com.hillel.ogm.git.util.JGitUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class GitManagerImpl<T> implements GitManager<T> {

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
        try {
            Field repo = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRepository.class)).findFirst().get();
            Field branch = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitRevision.class)).findFirst().get();
            repo.setAccessible(true);
            branch.setAccessible(true);
            String reposi = (String) repo.get(t);
            String branchi = (String) branch.get(t);
            initGitCloneToMap(t, reposi, branchi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private void initGitCloneToMap(T t, String repository, String revision) throws Exception {
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository("a", "a", "https://github.com/Hillelmed/Assembler.git");
        Map<String,String> res = loadRemote(inMemoryRepository,revision);
        Field f = Arrays.stream(t.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(GitFile.class)).findFirst().get();
        f.setAccessible(true);
        f.set(t,res);
    }

    private Map<String, String> loadRemote(InMemoryRepository repo,String revision) throws Exception {
        Git git = new Git(repo);
        git.fetch()
                .setRemote("https://github.com/Hillelmed/Assembler.git")
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                .call();
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
