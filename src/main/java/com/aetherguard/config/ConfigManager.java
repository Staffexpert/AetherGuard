package com.aetherguard.config;

import com.aetherguard.core.AetherGuard;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * AetherGuard Configuration Manager
 * Version: v1.2.0
 *
 * Handles all configuration files and settings for the anti-cheat system.
 * Designed for safe reloads, caching and compatibility across server versions.
 * Provides typed accessors for checks, profiles and performance tuning.
 *
 * Author: AetherGuard Team
 */
public class ConfigManager {
    
    private final AetherGuard plugin;
    
    // Configuration files
    private FileConfiguration mainConfig;
    private FileConfiguration checksConfig;
    private FileConfiguration messagesConfig;
    
    // Configuration files storage
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    
    // Cached values for performance
    private String detectionProfile;
    private boolean enabled;
    private boolean debugMode;
    private boolean testMode;
    
    public ConfigManager(AetherGuard plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all configuration files
     */
    public void loadConfigurations() {
        // Load main configuration
        loadMainConfig();
        
        // Load checks configuration
        loadChecksConfig();
        
        // Load messages configuration
        loadMessagesConfig();
        
        // Cache important values
        cacheValues();
        
        plugin.getLogger().info("§7Configuration files loaded successfully");
    }
    
    /**
     * Load main configuration
     */
    private void loadMainConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        mainConfig = plugin.getConfig();
        configs.put("config", mainConfig);
        configFiles.put("config", configFile);
    }
    
    /**
     * Load checks configuration
     */
    private void loadChecksConfig() {
        File checksFile = new File(plugin.getDataFolder(), "checks.yml");
        if (!checksFile.exists()) {
            plugin.saveResource("checks.yml", false);
        }
        
        checksConfig = YamlConfiguration.loadConfiguration(checksFile);
        configs.put("checks", checksConfig);
        configFiles.put("checks", checksFile);
    }
    
    /**
     * Load messages configuration
     */
    private void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        configs.put("messages", messagesConfig);
        configFiles.put("messages", messagesFile);
    }
    
    /**
     * Cache frequently used values
     */
    private void cacheValues() {
        detectionProfile = mainConfig.getString("core.detection-profile", "MEDIUM");
        enabled = mainConfig.getBoolean("core.enabled", true);
        debugMode = mainConfig.getBoolean("core.debug-mode", false);
        testMode = mainConfig.getBoolean("advanced.test-mode", false);
    }
    
    /**
     * Reload all configurations
     */
    public void reloadConfigurations() {
        // Reload main config
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();
        configs.put("config", mainConfig);
        
        // Reload other configs
        reloadConfig("checks");
        reloadConfig("messages");
        
        // Re-cache values
        cacheValues();
        
        plugin.getLogger().info("§7Configuration files reloaded");
    }
    
    /**
     * Reload a specific configuration file
     */
    public void reloadConfig(String name) {
        File file = configFiles.get(name);
        if (file != null && file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.put(name, config);
            plugin.getLogger().info("§7Configuration file '§f" + name + ".yml§7' reloaded");
        }
    }
    
    /**
     * Save a specific configuration file
     */
    public void saveConfig(String name) {
        File file = configFiles.get(name);
        FileConfiguration config = configs.get(name);
        
        if (file != null && config != null) {
            try {
                config.save(file);
                plugin.getLogger().info("§7Configuration file '§f" + name + ".yml§7' saved");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "§cCould not save configuration file '§f" + name + ".yml§c'", e);
            }
        }
    }
    
    /**
     * Save the main configuration file
     */
    public void saveMainConfig() {
        saveConfig("config");
        plugin.saveConfig();
    }

    /**
     * Save all managed configuration files to disk
     */
    public void saveAllConfigs() {
        for (String name : configFiles.keySet()) {
            saveConfig(name);
        }
    }
    
    /**
     * Get configuration by name
     */
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }
    
    /**
     * Get main configuration
     */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }
    
    /**
     * Get checks configuration
     */
    public FileConfiguration getChecksConfig() {
        return checksConfig;
    }
    
    /**
     * Get messages configuration
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    /**
     * Get a string value from messages with color codes
     */
    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("§cMessage not found: " + path);
            return "§cMessage not found: " + path;
        }
        return colorize(message);
    }
    
    /**
     * Get a formatted message with placeholders
     */
    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }
        return message;
    }
    
    /**
     * Get general prefix
     */
    public String getPrefix() {
        return getMessage("general.prefix");
    }
    
    /**
     * Colorize a string
     */
    public String colorize(String string) {
        if (string == null) return null;
        return string.replace("&", "§");
    }
    
    /**
     * Get detection profile
     */
    public String getDetectionProfile() {
        return detectionProfile;
    }
    
    /**
     * Set detection profile
     */
    public void setDetectionProfile(String profile) {
        this.detectionProfile = profile;
        mainConfig.set("core.detection-profile", profile);
        saveConfig("config");
    }
    
    /**
     * Check if anti-cheat is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set anti-cheat enabled state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        mainConfig.set("core.enabled", enabled);
        saveConfig("config");
        plugin.setAntiCheatEnabled(enabled);
    }
    
    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Set debug mode
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        mainConfig.set("core.debug-mode", debugMode);
        saveConfig("config");
        plugin.setDebugMode(debugMode);
    }
    
    /**
     * Check if test mode is enabled
     */
    public boolean isTestMode() {
        return testMode;
    }
    
    /**
     * Set test mode
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
        mainConfig.set("advanced.test-mode", testMode);
        saveConfig("config");
        plugin.setTestMode(testMode);
    }
    
    /**
     * Get check configuration
     */
    public CheckConfig getCheckConfig(String category, String checkName, String type) {
        String path = category + "." + checkName + "." + type;
        ConfigurationSection section = checksConfig.getConfigurationSection(path);
        
        if (section == null) {
            plugin.getLogger().warning("§cCheck configuration not found: " + path);
            return new CheckConfig(false, 1, 10, 3, "FLAG", "Configuration not found");
        }
        
        boolean enabled = section.getBoolean("enabled", true);
        int strictness = section.getInt("strictness", 1);
        int maxViolations = section.getInt("max-violations", 10);
        int bufferSize = section.getInt("buffer-size", 3);
        String punishment = section.getString("punishment", "FLAG");
        String description = section.getString("description", "No description");
        
        return new CheckConfig(enabled, strictness, maxViolations, bufferSize, punishment, description);
    }
    
    /**
     * Get profile multipliers
     */
    public ProfileMultipliers getProfileMultipliers(String profile) {
        ConfigurationSection section = mainConfig.getConfigurationSection("checks.profiles." + profile);
        if (section == null) {
            return new ProfileMultipliers(1.0, 1.0, 1.0);
        }
        
        double strictnessMultiplier = section.getDouble("strictness-multiplier", 1.0);
        double vlMultiplier = section.getDouble("vl-multiplier", 1.0);
        double bufferMultiplier = section.getDouble("buffer-multiplier", 1.0);
        
        return new ProfileMultipliers(strictnessMultiplier, vlMultiplier, bufferMultiplier);
    }
    
    /**
     * Get action configuration
     */
    public ActionConfig getActionConfig(String actionName) {
        String path = "action-manager.actions." + actionName;
        ConfigurationSection section = mainConfig.getConfigurationSection(path);
        
        if (section == null) {
            return new ActionConfig(false, "Default action", 0);
        }
        
        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message", "Default action message");
        int delay = section.getInt("delay", 0);
        
        return new ActionConfig(enabled, message, delay);
    }
    
    /**
     * Check if a check category is enabled
     */
    public boolean isCategoryEnabled(String category) {
        return checksConfig.getBoolean("categories." + category + ".enabled", true);
    }
    
    /**
     * Get category priority
     */
    public int getCategoryPriority(String category) {
        return checksConfig.getInt("categories." + category + ".priority", 1);
    }
    
    /**
     * Get performance settings
     */
    public PerformanceSettings getPerformanceSettings() {
        ConfigurationSection section = mainConfig.getConfigurationSection("core.optimization");
        if (section == null) {
            return new PerformanceSettings(18.0, 200, 30, true, 0);
        }
        
        double minTps = section.getDouble("min-tps", 18.0);
        int maxLatency = section.getInt("max-latency", 200);
        int profileInterval = section.getInt("profile-interval", 30);
        boolean asyncProcessing = section.getBoolean("async-processing", true);
        int threadPoolSize = section.getInt("thread-pool-size", 0);
        
        return new PerformanceSettings(minTps, maxLatency, profileInterval, asyncProcessing, threadPoolSize);
    }
    
    /**
     * Get logging configuration
     */
    public LoggingConfig getLoggingConfig() {
        ConfigurationSection section = mainConfig.getConfigurationSection("logging");
        if (section == null) {
            return new LoggingConfig(true, "JSON", "logs/", true, "10MB", 10, "daily");
        }
        
        boolean enabled = section.getBoolean("enabled", true);
        String format = section.getString("format", "JSON");
        String logPath = section.getString("files.violations", "logs/violations.log");
        boolean rotation = section.getBoolean("rotation.enabled", true);
        String maxSize = section.getString("rotation.max-size", "10MB");
        int maxFiles = section.getInt("rotation.max-files", 10);
        String interval = section.getString("rotation.interval", "daily");
        
        return new LoggingConfig(enabled, format, logPath, rotation, maxSize, maxFiles, interval);
    }
    
    /**
     * Check configuration data class
     */
    public static class CheckConfig {
        private final boolean enabled;
        private final int strictness;
        private final int maxViolations;
        private final int bufferSize;
        private final String punishment;
        private final String description;
        
        public CheckConfig(boolean enabled, int strictness, int maxViolations, int bufferSize, String punishment, String description) {
            this.enabled = enabled;
            this.strictness = strictness;
            this.maxViolations = maxViolations;
            this.bufferSize = bufferSize;
            this.punishment = punishment;
            this.description = description;
        }
        
        public boolean isEnabled() { return enabled; }
        public int getStrictness() { return strictness; }
        public int getMaxViolations() { return maxViolations; }
        public int getBufferSize() { return bufferSize; }
        public String getPunishment() { return punishment; }
        public String getDescription() { return description; }
    }
    
    /**
     * Profile multipliers data class
     */
    public static class ProfileMultipliers {
        private final double strictnessMultiplier;
        private final double vlMultiplier;
        private final double bufferMultiplier;
        
        public ProfileMultipliers(double strictnessMultiplier, double vlMultiplier, double bufferMultiplier) {
            this.strictnessMultiplier = strictnessMultiplier;
            this.vlMultiplier = vlMultiplier;
            this.bufferMultiplier = bufferMultiplier;
        }
        
        public double getStrictnessMultiplier() { return strictnessMultiplier; }
        public double getVlMultiplier() { return vlMultiplier; }
        public double getBufferMultiplier() { return bufferMultiplier; }
    }
    
    /**
     * Action configuration data class
     */
    public static class ActionConfig {
        private final boolean enabled;
        private final String message;
        private final int delay;
        
        public ActionConfig(boolean enabled, String message, int delay) {
            this.enabled = enabled;
            this.message = message;
            this.delay = delay;
        }
        
        public boolean isEnabled() { return enabled; }
        public String getMessage() { return message; }
        public int getDelay() { return delay; }
    }
    
    /**
     * Performance settings data class
     */
    public static class PerformanceSettings {
        private final double minTps;
        private final int maxLatency;
        private final int profileInterval;
        private final boolean asyncProcessing;
        private final int threadPoolSize;
        
        public PerformanceSettings(double minTps, int maxLatency, int profileInterval, boolean asyncProcessing, int threadPoolSize) {
            this.minTps = minTps;
            this.maxLatency = maxLatency;
            this.profileInterval = profileInterval;
            this.asyncProcessing = asyncProcessing;
            this.threadPoolSize = threadPoolSize;
        }
        
        public double getMinTps() { return minTps; }
        public int getMaxLatency() { return maxLatency; }
        public int getProfileInterval() { return profileInterval; }
        public boolean isAsyncProcessing() { return asyncProcessing; }
        public int getThreadPoolSize() { return threadPoolSize; }
    }
    
    /**
     * Logging configuration data class
     */
    public static class LoggingConfig {
        private final boolean enabled;
        private final String format;
        private final String logPath;
        private final boolean rotation;
        private final String maxSize;
        private final int maxFiles;
        private final String interval;
        
        public LoggingConfig(boolean enabled, String format, String logPath, boolean rotation, String maxSize, int maxFiles, String interval) {
            this.enabled = enabled;
            this.format = format;
            this.logPath = logPath;
            this.rotation = rotation;
            this.maxSize = maxSize;
            this.maxFiles = maxFiles;
            this.interval = interval;
        }
        
        public boolean isEnabled() { return enabled; }
        public String getFormat() { return format; }
        public String getLogPath() { return logPath; }
        public boolean isRotation() { return rotation; }
        public String getMaxSize() { return maxSize; }
        public int getMaxFiles() { return maxFiles; }
        public String getInterval() { return interval; }
    }
}