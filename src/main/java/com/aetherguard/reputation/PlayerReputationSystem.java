package com.aetherguard.reputation;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Player Reputation System
 * Exclusive Feature - Tracks player trustworthiness over time
 */
public class PlayerReputationSystem {
    
    private final Map<UUID, ReputationProfile> profiles;
    
    public PlayerReputationSystem() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public void recordViolation(Player player) {
        ReputationProfile profile = profiles.computeIfAbsent(player.getUniqueId(), 
            k -> new ReputationProfile());
        profile.violations++;
        profile.reputation = Math.max(0, profile.reputation - 5);
    }
    
    public void recordCleanHours(Player player) {
        ReputationProfile profile = profiles.computeIfAbsent(player.getUniqueId(), 
            k -> new ReputationProfile());
        profile.reputation = Math.min(100, profile.reputation + 1);
    }
    
    public double getReputation(Player player) {
        ReputationProfile profile = profiles.get(player.getUniqueId());
        return profile != null ? profile.reputation : 50.0;
    }
    
    public boolean isTrusted(Player player) {
        return getReputation(player) > 75.0;
    }
    
    static class ReputationProfile {
        double reputation = 50.0;
        int violations = 0;
    }
}
