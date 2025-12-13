package com.aetherguard.core.module;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.listeners.PlayerListener;
import com.aetherguard.managers.interfaces.IActionManager;
import com.aetherguard.managers.interfaces.IPlayerManager;
import com.aetherguard.managers.interfaces.IViolationManager;
import com.aetherguard.managers.ActionManager;
import com.aetherguard.managers.PlayerManager;
import com.aetherguard.managers.ViolationManager;

import java.util.concurrent.CompletableFuture;

/**
 * Core Module - Sistemas centrales del anticheat
 */
public class CoreModule implements IModule {
    private final AetherGuard plugin;
    private IPlayerManager playerManager;
    private IViolationManager violationManager;
    private IActionManager actionManager;

    public CoreModule(AetherGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "CoreModule";
    }

    @Override
    public CompletableFuture<Void> onEnable() {
        return CompletableFuture.runAsync(() -> {
            this.playerManager = new PlayerManager(plugin);
            plugin.getServiceContainer().register(IPlayerManager.class, playerManager);

            this.violationManager = new ViolationManager(plugin);
            plugin.getServiceContainer().register(IViolationManager.class, violationManager);

            this.actionManager = new ActionManager(plugin);
            plugin.getServiceContainer().register(IActionManager.class, actionManager);

            // Registrar listeners que dependen de este módulo
            plugin.getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);

            plugin.getAetherGuardLogger().info("§aCore Module enabled");
        });
    }

    @Override
    public CompletableFuture<Void> onDisable() {
        return CompletableFuture.runAsync(() -> {
            if (playerManager != null) {
                playerManager.saveAllData();
                playerManager.cleanup();
            }
            if (violationManager != null) {
                violationManager.cleanup();
            }
            plugin.getAetherGuardLogger().info("§cCore Module disabled");
        });
    }

    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    public IViolationManager getViolationManager() {
        return violationManager;
    }

    public IActionManager getActionManager() {
        return actionManager;
    }
}