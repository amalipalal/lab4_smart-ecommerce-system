package org.example.application;

import org.example.util.PerformanceTimer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApplicationCache {
    private final Map<String, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T>T getOrLoad(String key, Supplier<T> loader) {
//        T result = loader.get();
//        PerformanceTimer.stopAndPrint();
//        return result;
        T result = (T) cache.computeIfAbsent(key, k -> loader.get());
        PerformanceTimer.stopAndPrint();
        return result;
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
