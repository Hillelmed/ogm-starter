package io.github.hillelmed.ogm.starter.service;

import io.github.hillelmed.ogm.starter.annotation.GitRepository;
import io.github.hillelmed.ogm.starter.annotation.GitRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceTest {

    private ReflectionService<TestEntity> reflectionService;

    @BeforeEach
    void setUp() {
        reflectionService = new ReflectionService<>(TestEntity.class);
    }

    @Test
    void testExtractRepositoryAndBranch() {
        TestEntity testEntity = new TestEntity();
        AtomicReference<Field> fieldRepoAtomic = new AtomicReference<>();
        AtomicReference<Field> fieldBranchAtomic = new AtomicReference<>();

        reflectionService.extractRepositoryAndBranch(testEntity, fieldRepoAtomic, fieldBranchAtomic);

        assertNotNull(fieldRepoAtomic.get());
        assertNotNull(fieldBranchAtomic.get());
        assertEquals("repository", fieldRepoAtomic.get().getName());
        assertEquals("branch", fieldBranchAtomic.get().getName());
    }

    @Test
    void testSetRepositoryAndBranch() throws IllegalAccessException {
        TestEntity testEntity = new TestEntity();
        AtomicReference<Field> fieldRepoAtomic = new AtomicReference<>();
        AtomicReference<Field> fieldBranchAtomic = new AtomicReference<>();
        reflectionService.extractRepositoryAndBranch(testEntity, fieldRepoAtomic, fieldBranchAtomic);

        reflectionService.setRepositoryAndBranch(testEntity, fieldRepoAtomic, fieldBranchAtomic, "newRepo", "newBranch");

        assertEquals("newRepo", testEntity.getRepository());
        assertEquals("newBranch", testEntity.getBranch());
    }

    @Test
    void testCreateInstanceAndSetRepoAndRevision() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        TestEntity testEntity = reflectionService.createInstanceAndSetRepoAndRevision("newRepo", "newBranch");

        assertNotNull(testEntity);
        assertEquals("newRepo", testEntity.getRepository());
        assertEquals("newBranch", testEntity.getBranch());
    }

    // Test entity class for testing purposes
    static class TestEntity {
        @GitRepository
        private String repository;
        @GitRevision
        private String branch;

        public String getRepository() {
            return repository;
        }

        public String getBranch() {
            return branch;
        }
    }

}
