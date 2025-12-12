package com.aetherguard.prediction;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Cheating Prediction Engine
 * Exclusive Feature - Predicts cheating before it happens
 */
public class CheatingPrediction {
    
    private final Map<Player, PredictionProfile> profiles;
    
    public CheatingPrediction() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double predictCheatingProbability(Player player) {
        PredictionProfile profile = profiles.computeIfAbsent(player, p -> new PredictionProfile());
        
        double probability = 0.0;
        
        if (profile.suspiciousActionCount > 10) probability += 40.0;
        if (profile.anomalyScore > 50) probability += 30.0;
        if (profile.hasAbnormalPattern()) probability += 30.0;
        
        return Math.min(probability, 100.0);
    }
    
    public void recordSuspiciousAction(Player player) {
        profiles.computeIfAbsent(player, p -> new PredictionProfile()).suspiciousActionCount++;
    }
    
    static class PredictionProfile {
        int suspiciousActionCount = 0;
        double anomalyScore = 0.0;
        
        boolean hasAbnormalPattern() {
            return suspiciousActionCount > 5;
        }
    }
}
