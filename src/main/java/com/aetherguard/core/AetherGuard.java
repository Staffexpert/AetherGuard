package com.aetherguard.core;

import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.config.AppConfig;
import com.aetherguard.core.container.ServiceContainer;
import com.aetherguard.core.logging.AetherGuardLogger;
import com.aetherguard.core.module.*;
import com.aetherguard.core.monitoring.MonitoringService;
import com.aetherguard.managers.*;
import com.aetherguard.managers.interfaces.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * AetherGuard v3.0.0 - Premium Anti-Cheat Core
 *
 * Modular architecture with dependency injection, interface-based design,
 * and centralized configuration. Supports plugin reloading and graceful shutdown.
 *
 * @version 2.0.0
 */
public class AetherGuard extends JavaPlugin {
    private static AetherGuard instance;
    private static final Object SINGLETON_LOCK = new Object();

    private AetherGuardLogger logger;
    private ServiceContainer serviceContainer;
    private ModuleRegistry moduleRegistry;
    private MonitoringService monitoringService;

    private ScheduledExecutorService asyncExecutor;
    private ExecutorService blockingExecutor;

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean debugMode = new AtomicBoolean(false);
    private final AtomicBoolean testMode = new AtomicBoolean(false);

    @Override
    public void onEnable() {
        synchronized (SINGLETON_LOCK) {
            instance = this;
        }

        long startTime = System.currentTimeMillis();

        try {
            logger = new AetherGuardLogger(getLogger());
            serviceContainer = new ServiceContainer(getLogger());
            moduleRegistry = new ModuleRegistry(logger);

            initializeExecutors();
            serviceContainer.register(ScheduledExecutorService.class, asyncExecutor);
            serviceContainer.register(ExecutorService.class, blockingExecutor);
            serviceContainer.register(AetherGuardLogger.class, logger);

            loadConfiguration();
            monitoringService = new MonitoringService(logger);
            serviceContainer.register(MonitoringService.class, monitoringService);

            initializeManagers();
            checkForUpdatesAsync();

            long elapsed = System.currentTimeMillis() - startTime;
            logBanner();
            logger.info("§aPlugin initialized in " + elapsed + "ms");
            logger.info("§7Checks: §f" + getCheckManager().getTotalChecks());
            logger.info("§7Profile: §f" + getConfigManager().getDetectionProfile());

        } catch (Exception e) {
            if (logger != null) {
                logger.error("FATAL ERROR during initialization", e);
            } else {
                getLogger().log(Level.SEVERE, "FATAL ERROR", e);
            }
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (moduleRegistry != null) {
                moduleRegistry.disableAll().join();
            }

            shutdownExecutors();
            if (serviceContainer != null) {
                serviceContainer.clear();
            }

            if (logger != null) {
                logger.success("Plugin shutdown complete");
            } else {
                getLogger().log(Level.INFO, "Plugin shutdown complete");
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Shutdown error", e);
            } else {
                getLogger().log(Level.SEVERE, "Shutdown error", e);
            }
        }
    }

    private void initializeExecutors() {
        this.asyncExecutor = new ScheduledThreadPoolExecutor(
            AppConfig.Threading.ASYNC_THREAD_COUNT,
            r -> {
                Thread t = new Thread(r, AppConfig.Threading.ASYNC_THREAD_NAME);
                t.setDaemon(true);
                return t;
            }
        );
        this.blockingExecutor = Executors.newFixedThreadPool(
            AppConfig.Threading.BLOCKING_THREAD_COUNT,
            r -> {
                Thread t = new Thread(r, AppConfig.Threading.BLOCKING_THREAD_NAME);
                t.setDaemon(true);
                return t;
            }
        );
    }

    private void loadConfiguration() {
        // La configuración ahora se gestiona dentro del SystemModule
    }

    private void initializeManagers() {
        moduleRegistry.register(new CoreModule(this));
        moduleRegistry.register(new DetectionModule(this));
        moduleRegistry.register(new AnalyticsModule(this));
        moduleRegistry.register(new SystemModule(this));

        // Enable all modules
        moduleRegistry.enableAll().join();
    }

    private void checkForUpdatesAsync() {
        if (!getConfig().getBoolean("update-checker", AppConfig.Default.UPDATE_CHECKER_ENABLED)) {
            return;
        }

        blockingExecutor.submit(() -> {
            try {
                String current = getDescription().getVersion();
                String latest = fetchLatestVersionFromGitHub();
                if (latest != null && !latest.equals(current)) {
                    logger.warn("New version available: " + latest);
                }
            } catch (Exception e) {
                logger.debug("Update check failed: " + e.getMessage());
            }
        });
    }

    private String fetchLatestVersionFromGitHub() {
        try {
            java.net.URL url = new java.net.URL(AppConfig.Updates.GITHUB_API_URL);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(AppConfig.Updates.GITHUB_TIMEOUT_MS);
            conn.setReadTimeout(AppConfig.Updates.GITHUB_TIMEOUT_MS);

            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();
                int idx = json.indexOf("\"tag_name\":\"");
                if (idx != -1) {
                    int start = idx + 12;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch latest version: " + e.getMessage());
        }
        return null;
    }

    private void shutdownExecutors() throws InterruptedException {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            if (!asyncExecutor.awaitTermination(AppConfig.Executors.SHUTDOWN_TIMEOUT_ASYNC, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        }
        if (blockingExecutor != null && !blockingExecutor.isShutdown()) {
            blockingExecutor.shutdown();
            if (!blockingExecutor.awaitTermination(AppConfig.Executors.SHUTDOWN_TIMEOUT_BLOCKING, TimeUnit.SECONDS)) {
                blockingExecutor.shutdownNow();
            }
        }
    }

    private void logBanner() {
        logger.info("");
        logger.info("§d╔══════════════════════════════════════════════════════════════════╗");
        logger.info("§d║                                                                  ║");
        logger.info("§d║  █████╗ ███████╗████████╗██╗  ██╗███████╗██████╗  ██████╗ ██╗   ██╗ █████╗ ██████╗ ██████╗  ║");
        logger.info("§d║ ██╔══██╗██╔════╝╚══██╔══╝██║  ██║██╔════╝██╔══██╗██╔════╝ ██║   ██║██╔══██╗██╔══██╗██╔══██╗ ║");
        logger.info("§d║ ███████║█████╗     ██║   ███████║█████╗  ██████╔╝██║  ███╗██║   ██║███████║██████╔╝██║  ██║ ║");
        logger.info("§d║ ██╔══██║██╔══╝     ██║   ██╔══██║██╔══╝  ██╔══██╗██║   ██║██║   ██║██╔══██║██╔══██╗██║  ██║ ║");
        logger.info("§d║ ██║  ██║███████╗   ██║   ██║  ██║███████╗██║  ██║╚██████╔╝╚██████╔╝██║  ██║██║  ██║██████╔╝ ║");
        logger.info("§d║ ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝  ║");
        logger.info("§d║                                                                  ║");
        logger.info("§d╠══════════════════════════════════════════════════════════════════╣");
        logger.info("§d║                    🛡️  ANTI-CHEAT PREMIUM v" + AppConfig.VERSION + " 🛡️                     ║");
        logger.info("§d╠══════════════════════════════════════════════════════════════════╣");
        logger.info("§d║ ✨ Sistema de detección multi-capa avanzado                      ║");
        logger.info("§d║ 🧠 Análisis de comportamiento en tiempo real                     ║");
        logger.info("§d║ 🔄 Aprendizaje adaptativo y reconocimiento de patrones          ║");
        logger.info("§d║ 🎯 Baja tasa de falsos positivos con umbrales configurables      ║");
        logger.info("§d║ 🏗️  Arquitectura modular con inyección de dependencias           ║");
        logger.info("§d║ 🚀 Optimizado para un rendimiento excepcional                    ║");
        logger.info("§d║ 💎 Protección robusta y fiable para tu servidor                 ║");
        logger.info("§d╠══════════════════════════════════════════════════════════════════╣");
        logger.info("§d║                ✅ SISTEMA DE PROTECCIÓN ACTIVADO ✅                ║");
        logger.info("§d╚══════════════════════════════════════════════════════════════════╝");
        logger.info("");
        logger.info("§a🌟 Inicializando módulos del sistema...");
        logger.info("");
    }

    public static AetherGuard getInstance() {
        if (instance == null) {
            synchronized (SINGLETON_LOCK) {
                if (instance == null) {
                    throw new IllegalStateException("AetherGuard not initialized");
                }
            }
        }
        return instance;
    }

    public AetherGuardLogger getAetherGuardLogger() {
        return logger;
    }

    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    public MonitoringService getMonitoringService() {
        return monitoringService;
    }

    // Métodos de acceso a los managers (a través del ServiceContainer)

    public ConfigManager getConfigManager() {
        return serviceContainer.get(ConfigManager.class);
    }

    public CheckManager getCheckManager() {
        return serviceContainer.get(CheckManager.class);
    }

    public IPlayerManager getPlayerManager() {
        return serviceContainer.get(IPlayerManager.class);
    }

    public IViolationManager getViolationManager() {
        return serviceContainer.get(IViolationManager.class);
    }

    public IActionManager getActionManager() {
        return serviceContainer.get(IActionManager.class);
    }

    public BanWaveManager getBanWaveManager() {
        return serviceContainer.get(BanWaveManager.class);
    }

    public PredictiveAnalyticsManager getPredictiveAnalyticsManager() {
        return serviceContainer.get(PredictiveAnalyticsManager.class);
    }

    public AdaptiveLearningManager getAdaptiveLearningManager() {
        return serviceContainer.get(AdaptiveLearningManager.class);
    }

    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public ExecutorService getBlockingExecutor() {
        return blockingExecutor;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public ScheduledExecutorService getExecutorService() {
        return asyncExecutor;
    }

    public boolean isAntiCheatEnabled() { return enabled.get(); }
    public void setAntiCheatEnabled(boolean value) {
        enabled.set(value);
        getLogger().log(value ? Level.INFO : Level.WARNING,
            value ? "§aAnticheat enabled" : "§cAnticheat disabled");
    }

    public boolean isDebugMode() { return debugMode.get(); }
    public void setDebugMode(boolean value) {
        debugMode.set(value);
        getLogger().log(Level.INFO, value ? "§aDebug ON" : "§cDebug OFF");
    }

    public boolean isTestMode() { return testMode.get(); }
    public void setTestMode(boolean value) {
        testMode.set(value);
        getLogger().log(Level.INFO, value ? "§aTest mode ON" : "§cTest mode OFF");
    }

    public String getServerVersion() { return Bukkit.getVersion(); }
    public int getProtocolVersion() {
        String v = getServerVersion();
        if (v.contains("1.21")) return 767;
        if (v.contains("1.20")) return 763;
        if (v.contains("1.19")) return 759;
        if (v.contains("1.18")) return 757;
        if (v.contains("1.17")) return 755;
        if (v.contains("1.16")) return 735;
        if (v.contains("1.15")) return 550;
        if (v.contains("1.14")) return 452;
        if (v.contains("1.13")) return 393;
        if (v.contains("1.12")) return 338;
        if (v.contains("1.11")) return 315;
        if (v.contains("1.10")) return 210;
        if (v.contains("1.9")) return 107;
        return 47;
    }

    public boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    public long getLastTPS() { return monitoringService.getTPS(); }
    public double getMemoryUsagePercentage() { return monitoringService.getMemoryUsagePercentage(); }

    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryInfo(
            runtime.totalMemory() - runtime.freeMemory(),
            runtime.maxMemory(),
            runtime.totalMemory()
        );
    }

    public static class MemoryInfo {
        public final long used, max, total;

        public MemoryInfo(long used, long max, long total) {
            this.used = used;
            this.max = max;
            this.total = total;
        }

        public double getUsagePercentage() { return (double) used / max * 100.0; }
        public String formatUsed() { return formatBytes(used); }
        public String formatMax() { return formatBytes(max); }
        public String formatTotal() { return formatBytes(total); }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
            return String.format("%.1fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Deprecated(forRemoval = true, since = "1.2.0")
    public static class MemoryUsage extends MemoryInfo {
        public MemoryUsage(long used, long max, long total) { super(used, max, total); }
        public String getFormattedUsed() { return formatUsed(); }
        public String getFormattedMax() { return formatMax(); }
        public String getFormattedTotal() { return formatTotal(); }
    }

    // Placeholder methods for missing managers
    public PerformanceAnalyticsManager getPerformanceAnalyticsManager() {
        return serviceContainer.get(PerformanceAnalyticsManager.class);
    }

    public Object getCloudSyncManager() {
        return null; // Placeholder - not implemented yet
    }
}
