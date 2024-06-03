package io.github.hillelmed.ogm.starter.invocation;

import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Dynamic repository invocation handler.
 */
@Slf4j
public class DynamicRepositoryInvocationHandler implements InvocationHandler {

    private final Map<String, Method> methods = new HashMap<>();
    private final GitCrudRepositoryImpl gitRepository;


    /**
     * Instantiates a new Dynamic repository invocation handler.
     *
     * @param gitRepository the git repository
     */
    public DynamicRepositoryInvocationHandler(GitCrudRepositoryImpl gitRepository) {
        this.gitRepository = gitRepository;
        for (Method method : gitRepository.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                this.methods.put(method.getName(), method);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            return methods.get(method.getName()).invoke(gitRepository, args);
        } catch (IllegalAccessException | InvocationTargetException | OgmRuntimeException e) {
            if (e.getCause() instanceof OgmRuntimeException e1) {
                throw e1;
            }
            throw new OgmRuntimeException(e);
        }
    }

}
