package com.aetherguard.core.module;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PredictiveAnalyticsManager;
import com.aetherguard.managers.PerformanceAnalyticsManager;

import java.util.concurrent.CompletableFuture;

/**
 * Analytics Module - Agrupa sistemas de análisis y predicción
 */
public class AnalyticsModule implements IModule {
    private final AetherGuard plugin;
    private PredictiveAnalyticsManager predictiveAnalyticsManager;
    private PerformanceAnalyticsManager performanceAnalyticsManager;

    public AnalyticsModule(AetherGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "AnalyticsModule";
    }

    @Override
    public CompletableFuture<Void> onEnable() {
        return CompletableFuture.runAsync(() -> {
            this.predictiveAnalyticsManager = new PredictiveAnalyticsManager(plugin);
            plugin.getServiceContainer().register(PredictiveAnalyticsManager.class, predictiveAnalyticsManager);

            this.performanceAnalyticsManager = new PerformanceAnalyticsManager(plugin);
            plugin.getServiceContainer().register(PerformanceAnalyticsManager.class, performanceAnalyticsManager);

            plugin.getLogger().info("§aAnalytics Module enabled");
        });
    }

    @Override
    public CompletableFuture<Void> onDisable() {
        return CompletableFuture.runAsync(() -> {
            plugin.getLogger().info("§cAnalytics Module disabled");
        });
    }

    public PredictiveAnalyticsManager getPredictiveAnalyticsManager() {
        return predictiveAnalyticsManager;
    }

    public PerformanceAnalyticsManager getPerformanceAnalyticsManager() {
        return performanceAnalyticsManager;
    }
}