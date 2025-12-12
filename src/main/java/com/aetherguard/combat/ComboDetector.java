package com.aetherguard.combat;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Combat Combo Detector
 * Exclusive Feature - Detects inhuman combat combos
 */
public class ComboDetector {
    
    private final Map<Player, ComboProfile> profiles;
    
    public ComboDetector() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double detectCombo(Player player) {
        ComboProfile profile = profiles.computeIfAbsent(player, p -> new ComboProfile());
        profile.recordHit();
        
        if (profile.hitCount > 20) return 75.0;
        if (profile.isChained && profile.hitCount > 10) return 60.0;
        
        return 0.0;
    }
    
    static class ComboProfile {
        int hitCount = 0;
        boolean isChained = false;
        long lastHitTime = 0;
        
        void recordHit() {
            long now = System.currentTimeMillis();
            if (lastHitTime > 0 && (now - lastHitTime) < 200) {
                isChained = true;
            }
            hitCount++;
            lastHitTime = now;
            
            if (hitCount > 100) hitCount = 0;
        }
    }
}
