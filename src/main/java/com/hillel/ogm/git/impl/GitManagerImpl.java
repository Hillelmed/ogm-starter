package com.hillel.ogm.git.impl;

import com.hillel.ogm.git.GitManager;
import com.hillel.ogm.git.util.JGitUtil;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;

import java.util.Map;

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

    }

    private Map<String,String> initGitCloneToMap(Class<T> t, String repository, String revision) {
        InMemoryRepository inMemoryRepository = JGitUtil.getInMemoryRepository("a","a","v");
    }
}
