package com.aetherguard.core.module;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.CheckManager; 

import java.util.concurrent.CompletableFuture;

/**
 * Detection Module - Agrupa todos los sistemas de detección
 */
public class DetectionModule implements IModule {
    private final AetherGuard plugin;
    private CheckManager checkManager;

    public DetectionModule(AetherGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "DetectionModule";
    }

    @Override
    public CompletableFuture<Void> onEnable() {
        return CompletableFuture.runAsync(() -> {
            this.checkManager = new CheckManager(plugin);
            plugin.getServiceContainer().register(CheckManager.class, checkManager);

            plugin.getLogger().info("§aDetection Module enabled");
        });
    }

    @Override
    public CompletableFuture<Void> onDisable() {
        return CompletableFuture.runAsync(() -> {
            // Cleanup if needed
            plugin.getLogger().info("§cDetection Module disabled");
        });
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }
}