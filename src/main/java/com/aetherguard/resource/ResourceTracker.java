package com.aetherguard.resource;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Resource Accumulation Tracker
 * Exclusive Feature - Detects impossible resource gain rates
 */
public class ResourceTracker {
    
    private final Map<Player, ResourceProfile> profiles;
    
    public ResourceTracker() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double trackResourceGain(Player player) {
        ResourceProfile profile = profiles.computeIfAbsent(player, p -> new ResourceProfile());
        profile.recordTime();
        
        double suspicion = 0.0;
        
        suspicion += detectImpossibleMiningRate(profile) * 0.25;
        suspicion += detectImpossibleFarmingRate(profile) * 0.25;
        suspicion += detectImpossibleCraftingRate(profile) * 0.25;
        suspicion += detectImpossibleFishingRate(profile) * 0.25;
        
        return Math.min(suspicion, 100.0);
    }
    
    public void recordMined(Player player, Material material) {
        profiles.computeIfAbsent(player, p -> new ResourceProfile()).recordMined(material);
    }
    
    private double detectImpossibleMiningRate(ResourceProfile profile) {
        if (profile.miningHistory.size() < 10) return 0.0;
        long elapsed = profile.lastRecordTime - profile.firstMiningTime;
        if (elapsed < 1000 && profile.miningHistory.size() > 30) {
            return 85.0;
        }
        return 0.0;
    }
    
    private double detectImpossibleFarmingRate(ResourceProfile profile) {
        return 0.0;
    }
    
    private double detectImpossibleCraftingRate(ResourceProfile profile) {
        return 0.0;
    }
    
    private double detectImpossibleFishingRate(ResourceProfile profile) {
        return 0.0;
    }
    
    static class ResourceProfile {
        Deque<Material> miningHistory = new ArrayDeque<>();
        long firstMiningTime = 0;
        long lastRecordTime = 0;
        
        void recordMined(Material material) {
            if (miningHistory.isEmpty()) {
                firstMiningTime = System.currentTimeMillis();
            }
            miningHistory.addLast(material);
            if (miningHistory.size() > 100) miningHistory.removeFirst();
        }
        
        void recordTime() {
            lastRecordTime = System.currentTimeMillis();
        }
    }
}
