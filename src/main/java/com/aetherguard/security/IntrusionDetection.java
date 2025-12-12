package com.aetherguard.security;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Intrusion Detection System (IDS)
 * 
 * Monitors for injection attacks and system compromises
 * Similar to Grim's intrusion detection
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class IntrusionDetection {
    
    private final AetherGuard plugin;
    private final Map<Player, IntrustionProfile> profiles;
    private final List<String> suspiciousPatterns;
    
    public IntrusionDetection(AetherGuard plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
        this.suspiciousPatterns = initializeSuspiciousPatterns();
    }
    
    private List<String> initializeSuspiciousPatterns() {
        List<String> patterns = new ArrayList<>();
        patterns.add("java.lang.reflect");
        patterns.add("sun.reflect");
        patterns.add("classLoader");
        patterns.add("ClassPool");
        patterns.add("javassist");
        patterns.add("ASM");
        patterns.add("ByteBuddy");
        return patterns;
    }
    
    /**
     * Detect injection attacks
     */
    public double detectInjection(Player player, String data) {
        IntrustionProfile profile = profiles.computeIfAbsent(player, p -> new IntrustionProfile(p));
        
        double suspicion = 0.0;
        
        for (String pattern : suspiciousPatterns) {
            if (data.toLowerCase().contains(pattern.toLowerCase())) {
                suspicion += 50.0;
            }
        }
        
        if (suspicion > 0) {
            profile.injectionAttempts++;
        }
        
        return Math.min(suspicion, 100.0);
    }
    
    /**
     * Detect buffer overflow attempts
     */
    public double detectBufferOverflow(Player player, byte[] data, int maxSize) {
        if (data.length > maxSize) {
            IntrustionProfile profile = profiles.computeIfAbsent(player, p -> new IntrustionProfile(p));
            profile.overflowAttempts++;
            return 95.0;
        }
        return 0.0;
    }
    
    /**
     * Detect protocol manipulation
     */
    public double detectProtocolManipulation(Player player, String packetData) {
        IntrustionProfile profile = profiles.computeIfAbsent(player, p -> new IntrustionProfile(p));
        
        double suspicion = 0.0;
        
        if (packetData.contains("\0")) suspicion += 40.0;
        if (packetData.length() == 0) suspicion += 20.0;
        if (packetData.contains("%x")) suspicion += 60.0;
        
        if (suspicion > 0) {
            profile.manipulationAttempts++;
        }
        
        return suspicion;
    }
    
    /**
     * Check if player is suspicious
     */
    public boolean isSuspiciousPlayer(Player player) {
        IntrustionProfile profile = profiles.get(player);
        if (profile == null) return false;
        
        return profile.getTotalAttempts() > 5;
    }
    
    public void removePlayer(Player player) {
        profiles.remove(player);
    }
    
    static class IntrustionProfile {
        private final Player player;
        int injectionAttempts;
        int overflowAttempts;
        int manipulationAttempts;
        
        IntrustionProfile(Player player) {
            this.player = player;
            this.injectionAttempts = 0;
            this.overflowAttempts = 0;
            this.manipulationAttempts = 0;
        }
        
        int getTotalAttempts() {
            return injectionAttempts + overflowAttempts + manipulationAttempts;
        }
    }
}
