package io.github.hillelmed.ogm.framework;

import io.github.hillelmed.ogm.repository.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;

@Slf4j
@Data
@RequiredArgsConstructor
public class DynamicRepositoryInvocationHandler implements InvocationHandler {

    private final GitRepositoryImpl gitRepository;

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
            case "create":
                return gitRepository.create(args[0]);
            case "update":
                return gitRepository.update(args[0]);

            default:
                throw new IllegalStateException("Unexpected value: " + method.getName());
        }
        return null;
    }
}
