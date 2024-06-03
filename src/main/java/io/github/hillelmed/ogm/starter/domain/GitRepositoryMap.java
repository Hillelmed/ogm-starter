package io.github.hillelmed.ogm.starter.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The type Git repository map.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class GitRepositoryMap extends HashMap<String, String> {

    private final List<String> keyPathAddedOrChange = new ArrayList<>();
    private final List<String> keyPathDeleted = new ArrayList<>();

    /**
     * Instantiates a new Git repository map.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     */
    public GitRepositoryMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Instantiates a new Git repository map.
     *
     * @param initialCapacity the initial capacity
     */
    public GitRepositoryMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Instantiates a new Git repository map.
     */
    public GitRepositoryMap() {
    }

    /**
     * Instantiates a new Git repository map.
     *
     * @param m the m
     */
    public GitRepositoryMap(Map<String,String> m) {
        super(m);
        keyPathAddedOrChange.addAll(m.keySet());
    }

    /**
     * Remove all key path added or change boolean.
     *
     * @return the boolean
     */
    public boolean removeAllKeyPathAddedOrChange() {
        return keyPathAddedOrChange.removeAll(new ArrayList<>(keyPathAddedOrChange));
    }

    /**
     * Remove all key path deleted boolean.
     *
     * @return the boolean
     */
    public boolean removeAllKeyPathDeleted() {
        return keyPathDeleted.removeAll(new ArrayList<>(keyPathDeleted));
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (super.remove(key, value)) {
            keyPathDeleted.add((String) key);
            return true;
        }
        return false;
    }

    @Override
    public String remove(Object key) {
        String value = super.remove(key);
        if (value != null) {
            keyPathDeleted.add((String) key);
            return value;
        }
        return null;
    }

    @Override
    public boolean replace(String key, String oldValue, String newValue) {
        if (super.replace(key, oldValue, newValue)) {
            keyPathAddedOrChange.add(key);
            return true;
        }
        return false;
    }

    @Override
    public String replace(String key, String value) {
        String valueTmp = super.replace(key, value);
        if (valueTmp != null) {
            keyPathAddedOrChange.add(key);
            return valueTmp;
        }
        return null;
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        keyPathAddedOrChange.addAll(m.keySet());
        super.putAll(m);
    }

    @Override
    public String put(String key, String value) {
        String valueTmp = super.put(key, value);
        if (valueTmp == null || !valueTmp.equals(value)) {
            keyPathAddedOrChange.add(key);
            return valueTmp;
        }
        return null;
    }

    @Override
    public String putIfAbsent(String key, String value) {
        String valueTmp = super.putIfAbsent(key, value);
        if (valueTmp == null || !valueTmp.equals(value)) {
            keyPathAddedOrChange.add(key);
            return valueTmp;
        }
        return null;
    }

    @Override
    public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
        keyPathAddedOrChange.add(key);
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        keyPathAddedOrChange.add(key);
        return super.compute(key, remappingFunction);
    }

    @Override
    public String computeIfPresent(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        keyPathAddedOrChange.add(key);
        return super.computeIfPresent(key, remappingFunction);
    }
}
