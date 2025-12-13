package com.aetherguard.core.container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ServiceContainer {
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    private final Logger logger;

    public ServiceContainer(Logger logger) {
        this.logger = logger;
    }

    public <T> void register(Class<T> type, T instance) {
        singletons.put(type, instance);
    }

    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }

        if (factories.containsKey(type)) {
            Object instance = factories.get(type).get();
            singletons.put(type, instance);
            return (T) instance;
        }

        throw new IllegalStateException("Service not found: " + type.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getOptional(Class<T> type) {
        try {
            return get(type);
        } catch (IllegalStateException e) {
            logger.fine("Optional service not available: " + type.getName());
            return null;
        }
    }

    public <T> boolean isRegistered(Class<T> type) {
        return singletons.containsKey(type) || factories.containsKey(type);
    }

    public void clear() {
        singletons.clear();
        factories.clear();
    }
}
