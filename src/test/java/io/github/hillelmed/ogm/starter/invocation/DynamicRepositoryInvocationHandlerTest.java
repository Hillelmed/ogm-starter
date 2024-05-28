package io.github.hillelmed.ogm.starter.invocation;

import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepository;
import io.github.hillelmed.ogm.starter.repository.GitCrudRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicRepositoryInvocationHandlerTest {

    @Mock
    private GitCrudRepositoryImpl<Object> gitRepository;

    private DynamicRepositoryInvocationHandler handler;
    private GitCrudRepository proxy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new DynamicRepositoryInvocationHandler(gitRepository);
        proxy = (GitCrudRepository) Proxy.newProxyInstance(
                GitCrudRepository.class.getClassLoader(),
                new Class<?>[]{GitCrudRepository.class},
                handler);
    }

    @Test
    void testInvokeSync() throws Throwable {
        Method method = GitCrudRepository.class.getMethod("sync", Object.class);
        Object arg = new Object();
        doNothing().when(gitRepository).sync(arg);

        handler.invoke(proxy, method, new Object[]{arg});

        verify(gitRepository, times(1)).sync(arg);
    }

    @Test
    void testInvokeRead() throws Throwable {
        Method method = GitCrudRepository.class.getMethod("read", Object.class);
        Object arg = new Object();
        Object expectedResult = new Object();
        when(gitRepository.read(arg)).thenReturn(expectedResult);

        Object result = handler.invoke(proxy, method, new Object[]{arg});

        assertEquals(expectedResult, result);
        verify(gitRepository, times(1)).read(arg);
    }

    @Test
    void testInvokeUpdate() throws Throwable {
        Method method = GitCrudRepository.class.getMethod("update", Object.class);
        Object arg = new Object();
        doNothing().when(gitRepository).update(arg);

        handler.invoke(proxy, method, new Object[]{arg});

        verify(gitRepository, times(1)).update(arg);
    }

    @Test
    void testInvokeGetByRepositoryAndRevision() throws Throwable {
        Method method = GitCrudRepository.class.getMethod("getByRepositoryAndRevision", String.class, String.class);
        String repo = "repo";
        String revision = "revision";
        Object expectedResult = new Object();
        when(gitRepository.getByRepositoryAndRevision(repo, revision)).thenReturn(expectedResult);

        Object result = handler.invoke(proxy, method, new Object[]{repo, revision});

        assertEquals(expectedResult, result);
        verify(gitRepository, times(1)).getByRepositoryAndRevision(repo, revision);
    }

    @Test
    void testInvokeMethodThrowsOgmRuntimeException() throws Throwable {
        Method method = GitCrudRepository.class.getMethod("sync", Object.class);
        Object arg = new Object();
        doThrow(new OgmRuntimeException("test exception")).when(gitRepository).sync(arg);

        OgmRuntimeException thrown = assertThrows(OgmRuntimeException.class, () -> {
            handler.invoke(proxy, method, new Object[]{arg});
        });

        assertEquals("test exception", thrown.getMessage());
        verify(gitRepository, times(1)).sync(arg);
    }

}
