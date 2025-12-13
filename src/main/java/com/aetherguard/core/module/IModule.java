package com.aetherguard.core.module;

import java.util.concurrent.CompletableFuture;

public interface IModule {
    String getName();

    CompletableFuture<Void> onEnable();

    CompletableFuture<Void> onDisable();

    default void onReload() {
    }

    default boolean isEnabled() {
        return true;
    }
}
