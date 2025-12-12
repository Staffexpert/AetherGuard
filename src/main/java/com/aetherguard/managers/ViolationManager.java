package com.aetherguard.managers;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Violation Manager
 * 
 * Manages violation tracking and handling
 * Handles violation decay and punishment thresholds
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ViolationManager {
    
    private final AetherGuard plugin;
    private final Map<UUID, Map<String, ViolationData>> playerViolations;
    private final Map<UUID, List<ViolationRecord>> violationHistory;
    
    public ViolationManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.playerViolations = new ConcurrentHashMap<>();
        this.violationHistory = new ConcurrentHashMap<>();
    }
    
    /**
     * Add violation for player
     */
    public void addViolation(Player player, Check check, CheckResult result) {
        UUID uuid = player.getUniqueId();
        String checkName = check.getFullName();
        
        // Get or create violation data
        Map<String, ViolationData> violations = playerViolations.computeIfAbsent(uuid, 
            key -> new ConcurrentHashMap<>());
        
        ViolationData violationData = violations.computeIfAbsent(checkName, 
            key -> new ViolationData(checkName));
        
        // Add violation
        violationData.addViolation(result);
        
        // Add to history
        List<ViolationRecord> history = violationHistory.computeIfAbsent(uuid, 
            key -> new ArrayList<>());
        
        history.add(new ViolationRecord(
            System.currentTimeMillis(),
            checkName,
            result.getReason(),
            result.getConfidence()
        ));
        
        // Limit history size
        if (history.size() > 100) {
            history.remove(0);
        }
        
        // Check if punishment should be triggered
        if (violationData.getViolations() >= check.getMaxViolations()) {
            plugin.getActionManager().executeAction(player, check.getPunishment(), check, result);
            violationData.resetViolations();
        }
    }
    
    /**
     * Get violations for player
     */
    public Map<String, ViolationData> getViolations(Player player) {
        return playerViolations.getOrDefault(player.getUniqueId(), new HashMap<>());
    }
    
    /**
     * Get violations for specific check
     */
    public ViolationData getViolations(Player player, String checkName) {
        Map<String, ViolationData> violations = playerViolations.get(player.getUniqueId());
        return violations != null ? violations.get(checkName) : null;
    }
    
    /**
     * Get total violations for player
     */
    public int getTotalViolations(Player player) {
        Map<String, ViolationData> violations = playerViolations.get(player.getUniqueId());
        if (violations == null) return 0;
        
        return violations.values().stream()
                .mapToInt(ViolationData::getViolations)
                .sum();
    }
    
    /**
     * Get violation history for player
     */
    public List<ViolationRecord> getViolationHistory(Player player) {
        return violationHistory.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    /**
     * Reset violations for player
     */
    public void resetViolations(Player player) {
        UUID uuid = player.getUniqueId();
        
        Map<String, ViolationData> violations = playerViolations.get(uuid);
        if (violations != null) {
            violations.values().forEach(ViolationData::resetViolations);
        }
    }
    
    /**
     * Reset violations for specific check
     */
    public void resetViolations(Player player, String checkName) {
        Map<String, ViolationData> violations = playerViolations.get(player.getUniqueId());
        if (violations != null) {
            ViolationData violationData = violations.get(checkName);
            if (violationData != null) {
                violationData.resetViolations();
            }
        }
    }
    
    /**
     * Clean up old violations
     */
    public void cleanupOldViolations() {
        long currentTime = System.currentTimeMillis();
        double decayRate = plugin.getConfigManager().getMainConfig()
                .getDouble("violation-system.decay-rate", 2.0);
        
        playerViolations.forEach((uuid, violations) -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                return;
            }
            
            violations.forEach((checkName, violationData) -> {
                long timeSinceLastViolation = currentTime - violationData.getLastViolationTime();
                
                // Decay violations every minute
                if (timeSinceLastViolation > 60000) {
                    violationData.decayViolations(decayRate);
                }
            });
            
            // Remove empty violation data
            violations.entrySet().removeIf(entry -> entry.getValue().getViolations() <= 0);
        });
        
        // Remove players with no violations
        playerViolations.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Get violation statistics
     */
    public ViolationStats getStats() {
        int totalPlayers = playerViolations.size();
        int totalViolations = 0;
        int activeChecks = 0;
        
        for (Map<String, ViolationData> violations : playerViolations.values()) {
            activeChecks += violations.size();
            totalViolations += violations.values().stream()
                    .mapToInt(ViolationData::getViolations)
                    .sum();
        }
        
        return new ViolationStats(totalPlayers, totalViolations, activeChecks);
    }
    
    /**
     * Violation data container
     */
    public static class ViolationData {
        private final String checkName;
        private int violations;
        private long lastViolationTime;
        private final List<Long> violationTimes;
        
        public ViolationData(String checkName) {
            this.checkName = checkName;
            this.violations = 0;
            this.lastViolationTime = 0;
            this.violationTimes = new ArrayList<>();
        }
        
        public void addViolation(CheckResult result) {
            violations++;
            lastViolationTime = System.currentTimeMillis();
            violationTimes.add(lastViolationTime);
            
            // Keep only recent violations (last 10)
            if (violationTimes.size() > 10) {
                violationTimes.remove(0);
            }
        }
        
        public void decayViolations(double decayRate) {
            violations = Math.max(0, violations - (int) decayRate);
        }
        
        public void resetViolations() {
            violations = 0;
            lastViolationTime = 0;
            violationTimes.clear();
        }
        
        // Getters
        public String getCheckName() { return checkName; }
        public int getViolations() { return violations; }
        public long getLastViolationTime() { return lastViolationTime; }
        public List<Long> getViolationTimes() { return new ArrayList<>(violationTimes); }
    }
    
    /**
     * Violation record for history
     */
    public static class ViolationRecord {
        private final long timestamp;
        private final String checkName;
        private final String reason;
        private final double confidence;
        
        public ViolationRecord(long timestamp, String checkName, String reason, double confidence) {
            this.timestamp = timestamp;
            this.checkName = checkName;
            this.reason = reason;
            this.confidence = confidence;
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public String getCheckName() { return checkName; }
        public String getReason() { return reason; }
        public double getConfidence() { return confidence; }
    }
    
    /**
     * Violation statistics
     */
    public static class ViolationStats {
        private final int totalPlayers;
        private final int totalViolations;
        private final int activeChecks;
        
        public ViolationStats(int totalPlayers, int totalViolations, int activeChecks) {
            this.totalPlayers = totalPlayers;
            this.totalViolations = totalViolations;
            this.activeChecks = activeChecks;
        }
        
        // Getters
        public int getTotalPlayers() { return totalPlayers; }
        public int getTotalViolations() { return totalViolations; }
        public int getActiveChecks() { return activeChecks; }
    }
    
    /**
     * Cleanup method for resource management
     */
    public void cleanup() {
        playerViolations.clear();
        violationHistory.clear();
    }
}