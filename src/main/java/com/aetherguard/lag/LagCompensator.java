package com.aetherguard.lag;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Lag Compensator
 * Exclusive Feature - Adjusts detection for lag without losing accuracy
 */
public class LagCompensator {
    
    private final Map<Player, LagProfile> profiles;
    
    public LagCompensator() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double getCompensatedThreshold(Player player, double baseThreshold) {
        LagProfile profile = profiles.computeIfAbsent(player, p -> new LagProfile());
        double ping = getPing(player);
        double compensation = (ping / 50.0) * 0.1;
        return baseThreshold + compensation;
    }
    
    private double getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return ((Number) entityPlayer.getClass().getField("ping").get(entityPlayer)).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }
    
    static class LagProfile {
        double averagePing = 0;
        int measurements = 0;
    }
}
