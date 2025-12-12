package com.aetherguard.knockback;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Knockback Analyzer
 * Exclusive Feature - Analyzes knockback resistance and behavior
 */
public class KnockbackAnalyzer {
    
    private final Map<Player, KnockbackProfile> profiles;
    
    public KnockbackAnalyzer() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double analyzeKnockback(Player player, Vector appliedKnockback, Vector actualMovement) {
        KnockbackProfile profile = profiles.computeIfAbsent(player, p -> new KnockbackProfile());
        profile.recordKnockback(appliedKnockback, actualMovement);
        
        double resistanceLevel = calculateResistance(appliedKnockback, actualMovement);
        
        if (resistanceLevel > 0.9) return 85.0;
        if (resistanceLevel > 0.7) return 60.0;
        
        return 0.0;
    }
    
    private double calculateResistance(Vector applied, Vector actual) {
        if (applied.length() == 0) return 0.0;
        return 1.0 - (actual.length() / applied.length());
    }
    
    static class KnockbackProfile {
        Deque<Double> resistanceHistory = new ArrayDeque<>();
        
        void recordKnockback(Vector applied, Vector actual) {
            double resistance = 1.0 - (actual.length() / (applied.length() + 0.001));
            resistanceHistory.addLast(resistance);
            if (resistanceHistory.size() > 50) resistanceHistory.removeFirst();
        }
    }
}
