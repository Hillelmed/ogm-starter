package io.github.hillelmed.ogm.starter.invocation;

import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.repository.GitRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DynamicRepositoryInvocationHandler implements InvocationHandler {

    private final Map<String, Method> methods = new HashMap<>();
    private final GitRepositoryImpl gitRepository;


    public DynamicRepositoryInvocationHandler(GitRepositoryImpl gitRepository) {
        this.gitRepository = gitRepository;
        for (Method method : gitRepository.getClass().getDeclaredMethods()) {
            this.methods.put(method.getName(), method);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            return methods.get(method.getName()).invoke(gitRepository, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new OgmRuntimeException(e);
        }
    }

}
