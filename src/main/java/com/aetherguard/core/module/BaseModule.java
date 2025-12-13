package com.aetherguard.core.module;

import com.aetherguard.core.container.ServiceContainer;
import com.aetherguard.core.logging.AetherGuardLogger;
import java.util.concurrent.CompletableFuture;

public abstract class BaseModule implements IModule {
    protected final ServiceContainer serviceContainer;
    protected final AetherGuardLogger logger;
    private volatile boolean enabled = false;

    protected BaseModule(ServiceContainer serviceContainer, AetherGuardLogger logger) {
        this.serviceContainer = serviceContainer;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Void> onEnable() {
        return CompletableFuture.runAsync(() -> {
            try {
                initialize();
                enabled = true;
                logger.info("Module enabled: " + getName());
            } catch (Exception e) {
                logger.error("Failed to enable module: " + getName(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> onDisable() {
        return CompletableFuture.runAsync(() -> {
            try {
                shutdown();
                enabled = false;
                logger.info("Module disabled: " + getName());
            } catch (Exception e) {
                logger.error("Failed to disable module: " + getName(), e);
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected abstract void initialize() throws Exception;

    protected abstract void shutdown() throws Exception;

    protected <T> T getService(Class<T> type) {
        return serviceContainer.get(type);
    }

    protected <T> T getServiceOptional(Class<T> type) {
        return serviceContainer.getOptional(type);
    }
}
