package com.aetherguard.api;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🛡️ AetherGuard API
 * 
 * Public API for developers to interact with AetherGuard
 * Provides methods for accessing anti-cheat data and functionality
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class AetherGuardAPI {
    
    private final AetherGuard plugin;
    
    public AetherGuardAPI(AetherGuard plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if AetherGuard is enabled
     */
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    /**
     * Get AetherGuard version
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    /**
     * Get all checks
     */
    public List<Check> getAllChecks() {
        return plugin.getCheckManager().getAllChecks();
    }
    
    /**
     * Get check by name
     */
    public Check getCheck(String name) {
        return plugin.getCheckManager().getCheck(name);
    }
    
    /**
     * Get check by category and type
     */
    public Check getCheck(String category, String name, String type) {
        return plugin.getCheckManager().getCheck(category, name, type);
    }
    
    /**
     * Get checks by category
     */
    public List<Check> getChecksByCategory(String category) {
        return plugin.getCheckManager().getCategoryChecks(category);
    }
    
    /**
     * Get checks by type
     */
    public List<Check> getChecksByType(CheckType type) {
        return CheckType.getByCategory(type.getCategory()).length > 0 ? 
            java.util.Arrays.asList(CheckType.getByCategory(type.getCategory()))
                .stream()
                .filter(checkType -> checkType == type)
                .map(checkType -> plugin.getCheckManager().getCheck(
                    checkType.getCategory().name().toLowerCase(),
                    checkType.name().split("_")[1].toLowerCase(),
                    "A"
                ))
                .collect(java.util.stream.Collectors.toList()) : 
            java.util.Collections.emptyList();
    }
    
    /**
     * Enable/disable a check
     */
    public boolean setCheckEnabled(String checkName, boolean enabled) {
        return plugin.getCheckManager().setCheckEnabled(checkName, enabled);
    }
    
    /**
     * Check if a check is enabled
     */
    public boolean isCheckEnabled(String checkName) {
        Check check = plugin.getCheckManager().getCheck(checkName);
        return check != null && check.isEnabled();
    }
    
    /**
     * Get violations for player
     */
    public Map<String, Long> getViolations(Player player) {
        Map<String, Long> violations = new java.util.HashMap<>();
        
        for (Check check : plugin.getCheckManager().getEnabledChecks()) {
            if (check.getViolations() > 0) {
                violations.put(check.getFullName(), check.getViolations());
            }
        }
        
        return violations;
    }
    
    /**
     * Get violation count for specific check
     */
    public long getViolationCount(Player player, String checkName) {
        com.aetherguard.managers.ViolationManager.ViolationData violationData = 
            plugin.getViolationManager().getViolations(player, checkName);
        return violationData != null ? violationData.getViolations() : 0;
    }
    
    /**
     * Get total violation count for player
     */
    public long getTotalViolations(Player player) {
        return plugin.getViolationManager().getTotalViolations(player);
    }
    
    /**
     * Reset violations for player
     */
    public void resetViolations(Player player) {
        plugin.getViolationManager().resetViolations(player);
    }
    
    /**
     * Reset violations for specific check
     */
    public void resetViolations(Player player, String checkName) {
        plugin.getViolationManager().resetViolations(player, checkName);
    }
    
    /**
     * Check if player is exempt
     */
    public boolean isExempt(Player player) {
        return plugin.getPlayerManager().isExempt(player);
    }
    
    /**
     * Check if player is exempt from specific check
     */
    public boolean isExempt(Player player, String checkName) {
        return plugin.getPlayerManager().isExempt(player, checkName);
    }
    
    /**
     * Check if player is exempt from category
     */
    public boolean isExemptFromCategory(Player player, String category) {
        return plugin.getPlayerManager().isExempt(player, category);
    }
    
    /**
     * Add exemption for player
     */
    public void addExemption(Player player) {
        plugin.getPlayerManager().addExemption(player);
    }
    
    /**
     * Remove exemption for player
     */
    public void removeExemption(Player player) {
        plugin.getPlayerManager().removeExemption(player);
    }
    
    /**
     * Add exemption for specific check
     */
    public void addExemption(Player player, String checkName) {
        plugin.getPlayerManager().addExemption(player, checkName);
    }
    
    /**
     * Remove exemption for specific check
     */
    public void removeExemption(Player player, String checkName) {
        plugin.getPlayerManager().removeExemption(player, checkName);
    }
    
    /**
     * Execute a check manually
     */
    public CheckResult executeCheck(Player player, String checkName) {
        Check check = plugin.getCheckManager().getCheck(checkName);
        if (check == null) {
            return CheckResult.pass();
        }
        
        // Create basic check data
        com.aetherguard.checks.CheckData data = new com.aetherguard.checks.CheckData(player);
        
        return check.executeCheck(player, data);
    }
    
    /**
     * Execute a check with custom data
     */
    public CheckResult executeCheck(Player player, String checkName, com.aetherguard.checks.CheckData data) {
        Check check = plugin.getCheckManager().getCheck(checkName);
        if (check == null) {
            return CheckResult.pass();
        }
        
        return check.executeCheck(player, data);
    }
    
    /**
     * Get detection profile
     */
    public String getDetectionProfile() {
        return plugin.getConfigManager().getDetectionProfile();
    }
    
    /**
     * Set detection profile
     */
    public void setDetectionProfile(String profile) {
        plugin.getConfigManager().setDetectionProfile(profile);
    }
    
    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return plugin.isDebugMode();
    }
    
    /**
     * Enable/disable debug mode
     */
    public void setDebugMode(boolean enabled) {
        plugin.setDebugMode(enabled);
    }
    
    /**
     * Check if test mode is enabled
     */
    public boolean isTestMode() {
        return plugin.isTestMode();
    }
    
    /**
     * Enable/disable test mode
     */
    public void setTestMode(boolean enabled) {
        plugin.setTestMode(enabled);
    }
    
    /**
     * Get statistics
     */
    public APIStats getStats() {
        com.aetherguard.managers.ViolationManager.ViolationStats violationStats = 
            plugin.getViolationManager().getStats();
        
        int totalChecks = plugin.getCheckManager().getTotalChecks();
        int enabledChecks = plugin.getCheckManager().getEnabledChecksCount();
        
        return new APIStats(
            totalChecks,
            enabledChecks,
            violationStats.getTotalPlayers(),
            violationStats.getTotalViolations(),
            getTPS(),
            plugin.getServer().getOnlinePlayers().size()
        );
    }
    
    /**
     * Get server TPS using reflection for compatibility
     */
    private double getTPS() {
        try {
            Object tpsObj = plugin.getServer().getClass().getMethod("getTPS").invoke(plugin.getServer());
            if (tpsObj instanceof double[]) {
                double[] tps = (double[]) tpsObj;
                return tps[0];
            }
        } catch (Exception e) {
            // getTPS not available on this server version
        }
        return 20.0;
    }
    
    /**
     * Register custom event listener
     */
    public void registerListener(org.bukkit.event.Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    /**
     * Unregister event listener
     */
    public void unregisterListener(org.bukkit.event.Listener listener) {
        HandlerList.unregisterAll(listener);
    }
    
    /**
     * Get player data
     */
    public com.aetherguard.managers.PlayerManager.PlayerData getPlayerData(Player player) {
        return plugin.getPlayerManager().getPlayerData(player);
    }
    
    /**
     * Get custom data for player
     */
    public Object getCustomData(Player player, String key) {
        return plugin.getPlayerManager().getPlayerData(player).getCustomData().get(key);
    }
    
    /**
     * Set custom data for player
     */
    public void setCustomData(Player player, String key, Object value) {
        plugin.getPlayerManager().getPlayerData(player).getCustomData().put(key, value);
    }
    
    /**
     * API Statistics
     */
    public static class APIStats {
        private final int totalChecks;
        private final int enabledChecks;
        private final int playersWithViolations;
        private final int totalViolations;
        private final double tps;
        private final int onlinePlayers;
        
        public APIStats(int totalChecks, int enabledChecks, int playersWithViolations, 
                       int totalViolations, double tps, int onlinePlayers) {
            this.totalChecks = totalChecks;
            this.enabledChecks = enabledChecks;
            this.playersWithViolations = playersWithViolations;
            this.totalViolations = totalViolations;
            this.tps = tps;
            this.onlinePlayers = onlinePlayers;
        }
        
        // Getters
        public int getTotalChecks() { return totalChecks; }
        public int getEnabledChecks() { return enabledChecks; }
        public int getPlayersWithViolations() { return playersWithViolations; }
        public int getTotalViolations() { return totalViolations; }
        public double getTPS() { return tps; }
        public int getOnlinePlayers() { return onlinePlayers; }
    }
}