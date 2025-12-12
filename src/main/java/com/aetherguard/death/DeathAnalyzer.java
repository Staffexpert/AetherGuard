package com.aetherguard.death;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Death Analyzer
 * Exclusive Feature - Analyzes suspicious death patterns
 */
public class DeathAnalyzer {
    
    private final Map<Player, DeathProfile> profiles;
    
    public DeathAnalyzer() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public void recordDeath(Player player, String cause) {
        DeathProfile profile = profiles.computeIfAbsent(player, p -> new DeathProfile());
        profile.recordDeath(cause);
    }
    
    public double analyzeSuspiciousDeaths(Player player) {
        DeathProfile profile = profiles.get(player);
        if (profile == null) return 0.0;
        
        if (profile.deathCount > 100) return 50.0;
        
        return 0.0;
    }
    
    static class DeathProfile {
        int deathCount = 0;
        String lastCause;
        
        void recordDeath(String cause) {
            deathCount++;
            lastCause = cause;
        }
    }
}
