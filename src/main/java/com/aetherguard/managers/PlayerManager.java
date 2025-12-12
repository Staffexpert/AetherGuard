package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Player Manager
 * 
 * Manages player data and exemption states
 * Handles player profiles and tracking
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class PlayerManager {
    
    private final AetherGuard plugin;
    private final Map<UUID, PlayerData> playerData;
    private final Set<UUID> globallyExempt;
    private final Map<UUID, Set<String>> checkExemptions;
    private final Map<UUID, Set<String>> categoryExemptions;
    
    public PlayerManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.globallyExempt = ConcurrentHashMap.newKeySet();
        this.checkExemptions = new ConcurrentHashMap<>();
        this.categoryExemptions = new ConcurrentHashMap<>();
    }
    
    /**
     * Register player on join
     */
    public void registerPlayer(Player player) {
        getPlayerData(player);
    }
    
    /**
     * Unregister player on quit
     */
    public void unregisterPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        playerData.remove(uuid);
        globallyExempt.remove(uuid);
        checkExemptions.remove(uuid);
        categoryExemptions.remove(uuid);
    }
    
    /**
     * Get player data
     */
    public PlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), 
            uuid -> new PlayerData(player));
    }
    
    /**
     * Check if player is globally exempt
     */
    public boolean isExempt(Player player) {
        return globallyExempt.contains(player.getUniqueId()) ||
               player.hasPermission("aetherguard.bypass");
    }
    
    /**
     * Check if player is exempt from specific check
     */
    public boolean isExempt(Player player, String checkName) {
        UUID uuid = player.getUniqueId();
        Set<String> exemptions = checkExemptions.get(uuid);
        return exemptions != null && exemptions.contains(checkName);
    }
    
    /**
     * Check if player is exempt from category
     */
    public boolean isExemptFromCategory(Player player, String category) {
        UUID uuid = player.getUniqueId();
        Set<String> exemptions = categoryExemptions.get(uuid);
        return exemptions != null && exemptions.contains(category);
    }
    
    /**
     * Add global exemption
     */
    public void addExemption(Player player) {
        globallyExempt.add(player.getUniqueId());
    }
    
    /**
     * Remove global exemption
     */
    public void removeExemption(Player player) {
        globallyExempt.remove(player.getUniqueId());
    }
    
    /**
     * Add check exemption
     */
    public void addExemption(Player player, String checkName) {
        checkExemptions.computeIfAbsent(player.getUniqueId(), uuid -> new HashSet<>()).add(checkName);
    }
    
    /**
     * Remove check exemption
     */
    public void removeExemption(Player player, String checkName) {
        UUID uuid = player.getUniqueId();
        Set<String> exemptions = checkExemptions.get(uuid);
        if (exemptions != null) {
            exemptions.remove(checkName);
            if (exemptions.isEmpty()) {
                checkExemptions.remove(uuid);
            }
        }
    }
    
    /**
     * Add category exemption
     */
    public void addCategoryExemption(Player player, String category) {
        categoryExemptions.computeIfAbsent(player.getUniqueId(), uuid -> new HashSet<>()).add(category);
    }
    
    /**
     * Remove category exemption
     */
    public void removeCategoryExemption(Player player, String category) {
        UUID uuid = player.getUniqueId();
        Set<String> exemptions = categoryExemptions.get(uuid);
        if (exemptions != null) {
            exemptions.remove(category);
            if (exemptions.isEmpty()) {
                categoryExemptions.remove(uuid);
            }
        }
    }
    
    /**
     * Save all player data
     */
    public void saveAllData() {
        // TODO: Implement data saving
    }
    
    /**
     * Clean up offline player data
     */
    public void cleanup() {
        Iterator<Map.Entry<UUID, PlayerData>> iterator = playerData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = iterator.next();
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Player data container
     */
    public static class PlayerData {
        private final UUID uuid;
        private final String name;
        private final long joinTime;
        private long lastViolationTime;
        private long totalViolations;
        private final Map<String, Long> checkViolations;
        private final Map<String, Object> customData;
        
        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.joinTime = System.currentTimeMillis();
            this.lastViolationTime = 0;
            this.totalViolations = 0;
            this.checkViolations = new HashMap<>();
            this.customData = new HashMap<>();
        }
        
        // Getters and setters
        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public long getJoinTime() { return joinTime; }
        public long getLastViolationTime() { return lastViolationTime; }
        public void setLastViolationTime(long lastViolationTime) { this.lastViolationTime = lastViolationTime; }
        public long getTotalViolations() { return totalViolations; }
        public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }
        public Map<String, Long> getCheckViolations() { return checkViolations; }
        public Map<String, Object> getCustomData() { return customData; }
    }
}