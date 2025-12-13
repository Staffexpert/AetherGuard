package com.aetherguard.core.module;

import com.aetherguard.core.logging.AetherGuardLogger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModuleRegistry {
    private final Map<String, IModule> modules = new HashMap<>();
    private final AetherGuardLogger logger;

    public ModuleRegistry(AetherGuardLogger logger) {
        this.logger = logger;
    }

    public void register(IModule module) {
        modules.put(module.getName(), module);
        logger.debug("Module registered: " + module.getName());
    }

    public IModule getModule(String name) {
        return modules.get(name);
    }

    public Collection<IModule> getAllModules() {
        return modules.values();
    }

    public Collection<IModule> getEnabledModules() {
        return modules.values().stream().filter(IModule::isEnabled).collect(Collectors.toList());
    }

    public CompletableFuture<Void> enableAll() {
        return CompletableFuture.allOf(
            modules.values().stream().map(IModule::onEnable).toArray(CompletableFuture[]::new)
        );
    }

    public CompletableFuture<Void> disableAll() {
        return CompletableFuture.allOf(
            modules.values().stream().map(IModule::onDisable).toArray(CompletableFuture[]::new)
        );
    }

    public void reloadAll() {
        modules.values().forEach(IModule::onReload);
        logger.info("All modules reloaded");
    }

    public int getModuleCount() {
        return modules.size();
    }

    public int getEnabledModuleCount() {
        return (int) modules.values().stream().filter(IModule::isEnabled).count();
    }
}
