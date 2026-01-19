package org.example.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApplicationCache {
    private final Map<String, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T>T getOrLoad(String key, Supplier<T> loader) {
        return (T) cache.computeIfAbsent(key, k -> loader.get());
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void invalidateByPrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public void invalidateAll() {
        cache.clear();
    }
}
