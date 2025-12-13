package com.aetherguard.core.module;

import com.aetherguard.api.AetherGuardAPI;
import com.aetherguard.commands.CommandManager;
import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.gui.GUIManager;
import com.aetherguard.security.AntiDisablerSystem;

import java.util.concurrent.CompletableFuture;

/**
 * System Module - Gestiona componentes de sistema como la configuración, comandos y API.
 */
public class SystemModule implements IModule {
    private final AetherGuard plugin;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private AntiDisablerSystem antiDisablerSystem;
    private GUIManager guiManager;
    private AetherGuardAPI api;

    public SystemModule(AetherGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "SystemModule";
    }

    @Override
    public CompletableFuture<Void> onEnable() {
        return CompletableFuture.runAsync(() -> {
            // Configuración
            plugin.saveDefaultConfig();
            this.configManager = new ConfigManager(plugin);
            configManager.loadConfigurations();
            plugin.getServiceContainer().register(ConfigManager.class, configManager);

            // Comandos (Reemplazo del WebDashboard)
            this.commandManager = new CommandManager(plugin);
            commandManager.registerCommands();
            plugin.getServiceContainer().register(CommandManager.class, commandManager);

            // GUI
            this.guiManager = new GUIManager(plugin);
            plugin.getServiceContainer().register(GUIManager.class, guiManager);

            // Otros sistemas
            this.antiDisablerSystem = new AntiDisablerSystem(plugin);
            plugin.getServiceContainer().register(AntiDisablerSystem.class, antiDisablerSystem);

            this.api = new AetherGuardAPI(plugin);
            plugin.getServiceContainer().register(AetherGuardAPI.class, api);

            plugin.getAetherGuardLogger().info("§aSystem Module enabled");
        });
    }

    @Override
    public CompletableFuture<Void> onDisable() {
        return CompletableFuture.completedFuture(null); // No hay acciones de limpieza complejas
    }
}