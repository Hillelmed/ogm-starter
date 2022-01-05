package com.hillel.ogm.dao;


import com.hillel.ogm.annotation.GitModel;
import com.hillel.ogm.annotation.GitModelAnnotation;
import com.hillel.ogm.exception.MissingAnnotationException;
import com.hillel.ogm.git.GitManager;
import com.hillel.ogm.git.impl.GitManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

@Component
@Slf4j
public abstract class AbstractGitRepository<T> implements GitRepository<T> {

    private final Class<T> t;
    @Autowired
    private GitManagerImpl<T> gitManagerImpl;

    protected AbstractGitRepository() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) t;
        this.t = (Class) parameterizedType.getActualTypeArguments()[0];
    }

    @PostConstruct
    public void init() throws MissingAnnotationException {
        if(!validateModel(t)) {
            throw new MissingAnnotationException("Some annotation missing in git model");
        }
    }

    private boolean validateModel(Class<T> t) {
        Set<Class<?>> annotations = getGitAnnotationSet();
        if(!t.isAnnotationPresent(GitModel.class)) {
            log.error("Model :" + t.getName() + " missing GitModel annotation");
            return false;
        }
        return 2 <= Arrays.stream(t.getDeclaredFields()).filter(field -> annotations.contains(field.getAnnotations()[0].annotationType())).count();
    }

    private Set<Class<?>> getGitAnnotationSet() {
        Reflections reflections = new Reflections("com.hillel.ogm.annotation");
        return reflections.getTypesAnnotatedWith(GitModelAnnotation.class);
    }

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
        gitManagerImpl.load(t);
    }


}
