package io.github.hillelmed.ogm.git.impl;

import io.github.hillelmed.ogm.annotation.GitModel;
import io.github.hillelmed.ogm.annotation.GitModelAnnotation;
import io.github.hillelmed.ogm.config.OgmConfig;
import io.github.hillelmed.ogm.dao.AbstractGitRepository;
import io.github.hillelmed.ogm.dao.GitRepository;
import io.github.hillelmed.ogm.exception.MissingAnnotationException;
import io.github.hillelmed.ogm.git.GitManager;
import jakarta.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Set;

@Component
@Slf4j
public class GitManagerImpl<T> implements GitManager<T> {

    @Autowired
    private GitRepository<T> gitRepository;
    protected Class<T> t;

    protected GitManagerImpl() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) t;
        this.t = (Class) parameterizedType.getActualTypeArguments()[0];
    }

    @PostConstruct
    public void init() throws MissingAnnotationException {
        if (!validateModel(t)) {
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
        return gitRepository.getByRepositoryAndRevision(repository, revision);
    }

    @Override
    public T create(T t) {
        return gitRepository.create(t);
    }

    @Override
    public T update(T t) {
        return gitRepository.update(t);
    }

    @Override
    public T read(T t) {
        return gitRepository.read(t);
    }

    @Override
    public void load(T t) {
        try {
            gitRepository.load(t);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }


}
