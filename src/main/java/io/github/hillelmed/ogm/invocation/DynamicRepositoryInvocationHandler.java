package io.github.hillelmed.ogm.invocation;

import io.github.hillelmed.ogm.repository.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;

@Slf4j
public record DynamicRepositoryInvocationHandler(GitRepositoryImpl gitRepository) implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "getByRepositoryAndRevision":
                return gitRepository.getByRepositoryAndRevision((String) args[0], (String) args[1]);
            case "read":
                return gitRepository.read(args[0]);
            case "load":
                gitRepository.load(args[0]);
                break;
            case "sync":
                return gitRepository.sync(args[0]);
            case "update":
                return gitRepository.update(args[0]);
            default:
                throw new IllegalStateException("Unexpected value: " + method.getName());
        }
        return null;
    }
}
