package com.aetherguard.core;

import com.aetherguard.commands.CommandManager;
import com.aetherguard.config.ConfigManager;
import com.aetherguard.gui.GUIManager;
import com.aetherguard.listeners.PacketListener;
import com.aetherguard.listeners.PlayerListener;
import com.aetherguard.managers.*;
import com.aetherguard.api.AetherGuardAPI;
import com.aetherguard.security.AntiDisablerSystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🛡️ AetherGuard AntiCheat Main Class
 * 
 * The most complete and advanced open-source anti-cheat for Minecraft
 * Compatible with Spigot 1.8.x - 1.21.x, PaperMC, Purpur, Bukkit
 * 
 * Features:
 * - 100+ advanced checks across all categories
 * - Machine learning integration
 * - Web panel support
 * - Modular architecture
 * - High performance optimization
 * - Multi-version compatibility
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class AetherGuard extends JavaPlugin {
    
    // Singleton instance
    private static AetherGuard instance;
    
    // Core managers
    private ConfigManager configManager;
    private CheckManager checkManager;
    private PlayerManager playerManager;
    private ViolationManager violationManager;
    private ActionManager actionManager;
    private CommandManager commandManager;
    private GUIManager guiManager;
    
    // Security system
    private AntiDisablerSystem antiDisablerSystem;
    
    // API instance
    private AetherGuardAPI api;
    
    // Thread pool for async operations
    private ScheduledExecutorService executorService;
    
    // Plugin state
    private boolean enabled = true;
    private boolean debugMode = false;
    private boolean testMode = false;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize thread pool
        executorService = Executors.newScheduledThreadPool(4);
        
        // Load configuration
        loadConfiguration();
        
        // Initialize managers
        initializeManagers();
        
        // Register events
        registerEvents();
        
        // Register commands
        registerCommands();
        
        // Start performance monitoring
        startPerformanceMonitoring();
        
        // Log startup message
        getLogger().info("§6§lAetherGuard AntiCheat §7§l» §aSuccessfully enabled!");
        getLogger().info("§7Version: §f" + getDescription().getVersion());
        getLogger().info("§7Checks loaded: §f" + checkManager.getTotalChecks());
        getLogger().info("§7Detection profile: §f" + configManager.getDetectionProfile());
        
        // Check for updates
        checkForUpdates();
    }
    
    @Override
    public void onDisable() {
        // Shutdown executor service
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        
        // Save data
        if (playerManager != null) {
            playerManager.saveAllData();
        }
        
        // Cleanup
        if (violationManager != null) {
            violationManager.cleanup();
        }
        
        getLogger().info("§6§lAetherGuard AntiCheat §7§l» §cSuccessfully disabled!");
    }
    
    /**
     * Load configuration files
     */
    private void loadConfiguration() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfigurations();
        
        // Load detection profile
        String profile = configManager.getDetectionProfile();
        getLogger().info("§7Detection profile loaded: §f" + profile);
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        // Initialize check manager first
        checkManager = new CheckManager(this);
        
        // Initialize other managers
        playerManager = new PlayerManager(this);
        violationManager = new ViolationManager(this);
        actionManager = new ActionManager(this);
        commandManager = new CommandManager(this);
        guiManager = new GUIManager(this);
        
        // Initialize security systems
        antiDisablerSystem = new AntiDisablerSystem(this);
        
        // Initialize API
        api = new AetherGuardAPI(this);
        
        getLogger().info("§7All managers initialized successfully");
    }
    
    /**
     * Register event listeners
     */
    private void registerEvents() {
        // Register packet listener (if available)
        try {
            getServer().getPluginManager().registerEvents(new PacketListener(this), this);
        } catch (Exception e) {
            getLogger().warning("§cPacket listener could not be registered: " + e.getMessage());
        }
        
        // Register player listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("§7Event listeners registered");
    }
    
    /**
     * Register commands
     */
    private void registerCommands() {
        commandManager.registerCommands();
        getLogger().info("§7Commands registered");
    }
    
    /**
     * Start performance monitoring
     */
    private void startPerformanceMonitoring() {
        // Monitor TPS every 5 seconds
        executorService.scheduleAtFixedRate(() -> {
            try {
                Object tpsObj = getServer().getClass().getMethod("getTPS").invoke(getServer());
                if (tpsObj instanceof double[]) {
                    double[] tps = (double[]) tpsObj;
                    if (tps[0] < 15.0) {
                        getLogger().warning("§cLow TPS detected: " + String.format("%.2f", tps[0]));
                    }
                }
            } catch (Exception e) {
                // getTPS not available on this server version
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        // Cleanup old violations every minute
        executorService.scheduleAtFixedRate(() -> {
            if (violationManager != null) {
                violationManager.cleanupOldViolations();
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        // Auto-save player data every 5 minutes
        executorService.scheduleAtFixedRate(() -> {
            if (playerManager != null) {
                playerManager.saveAllData();
            }
        }, 300, 300, TimeUnit.SECONDS);
    }
    
    /**
     * Check for updates from GitHub releases
     */
    private void checkForUpdates() {
        if (getConfig().getBoolean("update-checker", true)) {
            executorService.submit(() -> {
                try {
                    String currentVersion = getDescription().getVersion();
                    String latestVersion = fetchLatestVersion();
                    
                    if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                        getLogger().warning("§e§l[UPDATE] §eA new version is available!");
                        getLogger().warning("§e§l[UPDATE] §eCurrent: §f" + currentVersion);
                        getLogger().warning("§e§l[UPDATE] §eLatest: §f" + latestVersion);
                        getLogger().warning("§e§l[UPDATE] §eDownload at: §fhttps://github.com/Staffexpert/AetherGuard-AntiCheat/releases");
                    } else {
                        getLogger().info("§7You are running the latest version of AetherGuard");
                    }
                } catch (Exception e) {
                    getLogger().fine("Update check failed: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Fetch latest version from GitHub API
     */
    private String fetchLatestVersion() {
        try {
            java.net.URL url = new java.net.URL("https://api.github.com/repos/Staffexpert/AetherGuard-AntiCheat/releases/latest");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "AetherGuard-AntiCheat");
            
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
                
                String jsonResponse = response.toString();
                int tagIndex = jsonResponse.indexOf("\"tag_name\":\"");
                if (tagIndex != -1) {
                    int startIndex = tagIndex + 12;
                    int endIndex = jsonResponse.indexOf("\"", startIndex);
                    return jsonResponse.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            getLogger().fine("Failed to fetch version: " + e.getMessage());
        }
        return null;
    }
    
    // Getters
    public static AetherGuard getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public CheckManager getCheckManager() {
        return checkManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public ViolationManager getViolationManager() {
        return violationManager;
    }
    
    public ActionManager getActionManager() {
        return actionManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    public AetherGuardAPI getAPI() {
        return api;
    }
    
    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }
    
    public AntiDisablerSystem getAntiDisablerSystem() {
        return antiDisablerSystem;
    }
    
    // State management
    public boolean isAntiCheatEnabled() {
        return enabled;
    }
    
    public void setAntiCheatEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            getLogger().info("§cAetherGuard has been disabled");
        } else {
            getLogger().info("§aAetherGuard has been enabled");
        }
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            getLogger().info("§aDebug mode has been enabled");
        } else {
            getLogger().info("§cDebug mode has been disabled");
        }
    }
    
    public boolean isTestMode() {
        return testMode;
    }
    
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
        if (testMode) {
            getLogger().info("§aTest mode has been enabled - No punishments will be issued");
        } else {
            getLogger().info("§cTest mode has been disabled - Normal punishments resumed");
        }
    }
    
    /**
     * Get server version string
     */
    public String getServerVersion() {
        return Bukkit.getVersion();
    }
    
    /**
     * Get protocol version
     */
    public int getProtocolVersion() {
        // Simplified version detection
        String version = getServerVersion();
        if (version.contains("1.8")) return 47;
        if (version.contains("1.9")) return 107;
        if (version.contains("1.10")) return 210;
        if (version.contains("1.11")) return 315;
        if (version.contains("1.12")) return 338;
        if (version.contains("1.13")) return 393;
        if (version.contains("1.14")) return 452;
        if (version.contains("1.15")) return 550;
        if (version.contains("1.16")) return 735;
        if (version.contains("1.17")) return 755;
        if (version.contains("1.18")) return 757;
        if (version.contains("1.19")) return 759;
        if (version.contains("1.20")) return 763;
        if (version.contains("1.21")) return 767;
        return 47; // Default to 1.8
    }
    
    /**
     * Check if a plugin is present
     */
    public boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
    
    /**
     * Get memory usage information
     */
    public MemoryUsage getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new MemoryUsage(usedMemory, maxMemory, totalMemory);
    }
    
    /**
     * Memory usage data class
     */
    public static class MemoryUsage {
        private final long used;
        private final long max;
        private final long total;
        
        public MemoryUsage(long used, long max, long total) {
            this.used = used;
            this.max = max;
            this.total = total;
        }
        
        public long getUsed() { return used; }
        public long getMax() { return max; }
        public long getTotal() { return total; }
        
        public double getUsagePercentage() {
            return (double) used / max * 100;
        }
        
        public String getFormattedUsed() {
            return formatBytes(used);
        }
        
        public String getFormattedMax() {
            return formatBytes(max);
        }
        
        public String getFormattedTotal() {
            return formatBytes(total);
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}