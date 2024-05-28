package io.github.hillelmed.ogm.starter.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class GitRepositoryMapTest {

    private GitRepositoryMap gitRepositoryMap;

    @BeforeEach
    void setUp() {
        gitRepositoryMap = new GitRepositoryMap();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(gitRepositoryMap);
        assertTrue(gitRepositoryMap.isEmpty());
    }

    @Test
    void testConstructorWithInitialCapacity() {
        gitRepositoryMap = new GitRepositoryMap(10);
        assertNotNull(gitRepositoryMap);
        assertTrue(gitRepositoryMap.isEmpty());
    }

    @Test
    void testConstructorWithInitialCapacityAndLoadFactor() {
        gitRepositoryMap = new GitRepositoryMap(10, 0.75f);
        assertNotNull(gitRepositoryMap);
        assertTrue(gitRepositoryMap.isEmpty());
    }

    @Test
    void testConstructorWithMap() {
        Map<String, String> initialMap = new HashMap<>();
        initialMap.put("key1", "value1");
        initialMap.put("key2", "value2");

        gitRepositoryMap = new GitRepositoryMap(initialMap);
        assertNotNull(gitRepositoryMap);
        assertEquals(2, gitRepositoryMap.size());
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().containsAll(initialMap.keySet()));
    }

    @Test
    void testPut() {
        String result = gitRepositoryMap.put("key1", "value1");
        assertNull(result);
        assertEquals("value1", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testPutIfAbsent() {
        String result = gitRepositoryMap.putIfAbsent("key1", "value1");
        assertNull(result);
        assertEquals("value1", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testRemoveByKey() {
        gitRepositoryMap.put("key1", "value1");
        String removedValue = gitRepositoryMap.remove("key1");
        assertEquals("value1", removedValue);
        assertNull(gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathDeleted().contains("key1"));
    }

    @Test
    void testRemoveByKeyAndValue() {
        gitRepositoryMap.put("key1", "value1");
        boolean result = gitRepositoryMap.remove("key1", "value1");
        assertTrue(result);
        assertNull(gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathDeleted().contains("key1"));
    }

    @Test
    void testReplace() {
        gitRepositoryMap.put("key1", "value1");
        String result = gitRepositoryMap.replace("key1", "value2");
        assertEquals("value1", result);
        assertEquals("value2", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testReplaceWithOldAndNewValue() {
        gitRepositoryMap.put("key1", "value1");
        boolean result = gitRepositoryMap.replace("key1", "value1", "value2");
        assertTrue(result);
        assertEquals("value2", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testPutAll() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        gitRepositoryMap.putAll(map);
        assertEquals(2, gitRepositoryMap.size());
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().containsAll(map.keySet()));
    }

    @Test
    void testRemoveAllKeyPathAddedOrChange() {
        gitRepositoryMap.put("key1", "value1");
        assertFalse(gitRepositoryMap.getKeyPathAddedOrChange().isEmpty());

        boolean result = gitRepositoryMap.removeAllKeyPathAddedOrChange();
        assertTrue(result);
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().isEmpty());
    }

    @Test
    void testRemoveAllKeyPathDeleted() {
        gitRepositoryMap.put("key1", "value1");
        gitRepositoryMap.remove("key1");
        assertFalse(gitRepositoryMap.getKeyPathDeleted().isEmpty());

        boolean result = gitRepositoryMap.removeAllKeyPathDeleted();
        assertTrue(result);
        assertTrue(gitRepositoryMap.getKeyPathDeleted().isEmpty());
    }

    @Test
    void testComputeIfAbsent() {
        gitRepositoryMap.computeIfAbsent("key1", k -> "value1");
        assertEquals("value1", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testCompute() {
        gitRepositoryMap.put("key1", "value1");
        gitRepositoryMap.compute("key1", (k, v) -> "value2");
        assertEquals("value2", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testComputeIfPresent() {
        gitRepositoryMap.put("key1", "value1");
        gitRepositoryMap.computeIfPresent("key1", (k, v) -> "value2");
        assertEquals("value2", gitRepositoryMap.get("key1"));
        assertTrue(gitRepositoryMap.getKeyPathAddedOrChange().contains("key1"));
    }

    @Test
    void testReplaceAllUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> gitRepositoryMap.replaceAll((k, v) -> v + "modified"));
    }
}
