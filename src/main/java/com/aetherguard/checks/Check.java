package com.aetherguard.checks;

import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 🛡️ AetherGuard Check Base Class
 * 
 * Base class for all anti-cheat checks
 * Provides common functionality and interface implementation
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class Check {
    
    protected final AetherGuard plugin;
    protected final String category;
    protected final String name;
    protected final String type;
    protected final String fullName;
    
    // Check configuration
    protected boolean enabled;
    protected int strictness;
    protected int maxViolations;
    protected int bufferSize;
    protected String punishment;
    protected String description;
    
    // Check state
    protected final AtomicLong violations;
    protected final AtomicLong flags;
    protected long lastFlagTime;
    protected long lastViolationTime;
    
    // Performance tracking
    private long totalExecutionTime;
    private long executionCount;
    private long lastExecutionTime;
    
    public Check(AetherGuard plugin, String category, String name, String type) {
        this.plugin = plugin;
        this.category = category;
        this.name = name;
        this.type = type;
        this.fullName = category + "." + name + "." + type;
        
        this.violations = new AtomicLong(0);
        this.flags = new AtomicLong(0);
        this.lastFlagTime = 0;
        this.lastViolationTime = 0;
        
        this.totalExecutionTime = 0;
        this.executionCount = 0;
        this.lastExecutionTime = 0;
    }
    
    /**
     * Check method to be implemented by each specific check
     */
    public abstract CheckResult check(Player player, CheckData data);
    
    /**
     * Get check type for categorization
     */
    public abstract CheckType getCheckType();
    
    /**
     * Set check configuration
     */
    public void setConfig(ConfigManager.CheckConfig config) {
        this.enabled = config.isEnabled();
        this.strictness = config.getStrictness();
        this.maxViolations = config.getMaxViolations();
        this.bufferSize = config.getBufferSize();
        this.punishment = config.getPunishment();
        this.description = config.getDescription();
        
        // Apply profile multipliers
        applyProfileMultipliers();
    }
    
    /**
     * Apply detection profile multipliers
     */
    private void applyProfileMultipliers() {
        String profile = plugin.getConfigManager().getDetectionProfile();
        ConfigManager.ProfileMultipliers multipliers = plugin.getConfigManager().getProfileMultipliers(profile);
        
        this.strictness = (int) (strictness * multipliers.getStrictnessMultiplier());
        this.maxViolations = (int) (maxViolations * multipliers.getVlMultiplier());
        this.bufferSize = (int) (bufferSize * multipliers.getBufferMultiplier());
    }
    
    /**
     * Execute check with performance tracking
     */
    public CheckResult executeCheck(Player player, CheckData data) {
        if (!enabled || !plugin.isAntiCheatEnabled()) {
            return CheckResult.PASS;
        }
        
        long startTime = System.nanoTime();
        
        try {
            CheckResult result = check(player, data);
            
            if (result != CheckResult.PASS) {
                onFlag(player, result);
            }
            
            return result;
            
        } catch (Exception e) {
            plugin.getLogger().warning("§cError in check " + fullName + ": " + e.getMessage());
            return CheckResult.PASS;
        } finally {
            long executionTime = System.nanoTime() - startTime;
            updatePerformanceStats(executionTime);
        }
    }
    
    /**
     * Handle check flag
     */
    protected void onFlag(Player player, CheckResult result) {
        long currentTime = System.currentTimeMillis();
        
        // Update flag statistics
        flags.incrementAndGet();
        lastFlagTime = currentTime;
        
        // Increment violations if needed
        if (result.isViolation()) {
            long newViolations = violations.incrementAndGet();
            lastViolationTime = currentTime;
            
            // Update check manager stats
            plugin.getCheckManager().incrementCheckFlag(fullName);
            
            // Handle violation
            if (newViolations >= maxViolations) {
                onViolationThreshold(player, result);
            }
            
            // Log flag
            logFlag(player, result, newViolations);
            
            // Send alerts
            sendAlert(player, result, newViolations);
        }
        
        // Debug information
        if (plugin.isDebugMode()) {
            sendDebugInfo(player, result);
        }
    }
    
    /**
     * Handle violation threshold
     */
    protected void onViolationThreshold(Player player, CheckResult result) {
        if (!plugin.isTestMode()) {
            plugin.getActionManager().executeAction(player, punishment, this, result);
        }
        
        // Reset violations if configured
        if (plugin.getConfigManager().getMainConfig().getBoolean("violation-system.buffer.reset-on-punish", true)) {
            violations.set(0);
        }
    }
    
    /**
     * Log flag to file
     */
    protected void logFlag(Player player, CheckResult result, long currentViolations) {
        ConfigManager.LoggingConfig loggingConfig = plugin.getConfigManager().getLoggingConfig();
        if (!loggingConfig.isEnabled()) return;
        
        String logMessage = String.format(
            "[%s] %s (%s) failed %s - VL: %d/%d - Info: %s",
            new java.util.Date().toString(),
            player.getName(),
            player.getUniqueId().toString(),
            fullName,
            currentViolations,
            maxViolations,
            result.getDebugInfo()
        );
        
        plugin.getLogger().info("§7[FLAG] " + logMessage);
        
        // TODO: Write to log file
    }
    
    /**
     * Send alert to staff
     */
    protected void sendAlert(Player player, CheckResult result, long currentViolations) {
        String message = plugin.getConfigManager().getMessage(
            "alerts.violation",
            "player", player.getName(),
            "check", fullName,
            "vl", String.valueOf(currentViolations),
            "type", result.getAlertType().name()
        );
        
        // Send to all staff with permission
        plugin.getServer().getOnlinePlayers().forEach(staff -> {
            if (staff.hasPermission("aetherguard.alerts")) {
                staff.sendMessage(message);
            }
        });
        
        // Console alert
        plugin.getLogger().info("§c[ALEXERT] " + message);
    }
    
    /**
     * Send debug information
     */
    protected void sendDebugInfo(Player player, CheckResult result) {
        String debugMessage = plugin.getConfigManager().getMessage(
            "alerts.violation-debug",
            "player", player.getName(),
            "check", fullName,
            "vl", String.valueOf(violations.get()),
            "type", result.getAlertType().name(),
            "debug", result.getDebugInfo()
        );
        
        plugin.getLogger().info("§8[DEBUG] " + debugMessage);
    }
    
    /**
     * Update performance statistics
     */
    private void updatePerformanceStats(long executionTime) {
        totalExecutionTime += executionTime;
        executionCount++;
        lastExecutionTime = executionTime;
    }
    
    /**
     * Reset violations
     */
    public void resetViolations() {
        violations.set(0);
        flags.set(0);
        lastFlagTime = 0;
        lastViolationTime = 0;
    }
    
    /**
     * Decay violations over time
     */
    public void decayViolations() {
        long currentTime = System.currentTimeMillis();
        double decayRate = plugin.getConfigManager().getMainConfig().getDouble("violation-system.decay-rate", 2.0);
        
        // Decay violations every minute
        if (currentTime - lastViolationTime > 60000) {
            long currentVl = violations.get();
            if (currentVl > 0) {
                long newVl = Math.max(0, currentVl - (long) decayRate);
                violations.set(newVl);
            }
        }
    }
    
    /**
     * Check if player should be exempt from this check
     */
    protected boolean isExempt(Player player) {
        // Check global exemption
        if (plugin.getPlayerManager().isExempt(player)) {
            return true;
        }
        
        // Check check-specific exemption
        if (plugin.getPlayerManager().isExempt(player, fullName)) {
            return true;
        }
        
        // Check category exemption
        if (plugin.getPlayerManager().isExempt(player, category)) {
            return true;
        }
        
        // Check permission exemption
        if (player.hasPermission("aetherguard.bypass") || 
            player.hasPermission("aetherguard.bypass." + category) ||
            player.hasPermission("aetherguard.bypass." + fullName)) {
            return true;
        }
        
        return false;
    }
    
    // Getters
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getFullName() { return fullName; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getStrictness() { return strictness; }
    public int getMaxViolations() { return maxViolations; }
    public int getBufferSize() { return bufferSize; }
    public String getPunishment() { return punishment; }
    public String getDescription() { return description; }
    
    public long getViolations() { return violations.get(); }
    public long getFlags() { return flags.get(); }
    public long getLastFlagTime() { return lastFlagTime; }
    public long getLastViolationTime() { return lastViolationTime; }
    
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public long getExecutionCount() { return executionCount; }
    public long getLastExecutionTime() { return lastExecutionTime; }
    
    /**
     * Get average execution time in nanoseconds
     */
    public double getAverageExecutionTime() {
        return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0;
    }
    
    /**
     * Get average execution time in milliseconds
     */
    public double getAverageExecutionTimeMs() {
        return getAverageExecutionTime() / 1_000_000.0;
    }
    
    @Override
    public String toString() {
        return String.format("Check{fullName='%s', enabled=%s, violations=%d, strictness=%d}",
                fullName, enabled, violations.get(), strictness);
    }
}