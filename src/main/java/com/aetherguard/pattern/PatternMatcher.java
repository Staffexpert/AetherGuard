package com.aetherguard.pattern;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Pattern Matcher
 * 
 * Advanced pattern recognition for cheating behavior
 * Similar to Spartan's pattern detection
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class PatternMatcher {
    
    private final Map<Player, PatternProfile> profiles;
    
    public PatternMatcher() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    /**
     * Check if player matches known cheat patterns
     */
    public double matchCheatPattern(Player player, String patternType, double... values) {
        PatternProfile profile = profiles.computeIfAbsent(player, p -> new PatternProfile(p));
        profile.recordPattern(patternType, values);
        
        return analyzePattern(patternType, values, profile);
    }
    
    private double analyzePattern(String patternType, double[] values, PatternProfile profile) {
        double confidence = 0.0;
        
        switch (patternType) {
            case "PERFECT_CPS":
                confidence = detectPerfectCPS(values);
                break;
            case "LINEAR_AIM":
                confidence = detectLinearAim(values);
                break;
            case "CONSTANT_SPEED":
                confidence = detectConstantSpeed(values);
                break;
            case "ZERO_JITTER":
                confidence = detectZeroJitter(values);
                break;
            case "INSTANT_REACTION":
                confidence = detectInstantReaction(values);
                break;
            case "PERFECT_PREDICTION":
                confidence = detectPerfectPrediction(values);
                break;
        }
        
        return Math.min(confidence, 100.0);
    }
    
    private double detectPerfectCPS(double[] values) {
        if (values.length < 10) return 0.0;
        
        Set<Double> uniqueIntervals = new HashSet<>();
        for (double v : values) uniqueIntervals.add(v);
        
        if (uniqueIntervals.size() == 1) return 95.0;
        if (uniqueIntervals.size() <= 2) return 70.0;
        
        return 0.0;
    }
    
    private double detectLinearAim(double[] values) {
        if (values.length < 5) return 0.0;
        
        double avgDiff = 0;
        for (int i = 1; i < values.length; i++) {
            avgDiff += Math.abs(values[i] - values[i-1]);
        }
        avgDiff /= values.length - 1;
        
        if (avgDiff < 0.1) return 85.0;
        if (avgDiff < 0.5) return 60.0;
        
        return 0.0;
    }
    
    private double detectConstantSpeed(double[] values) {
        if (values.length < 10) return 0.0;
        
        double variance = 0;
        double mean = 0;
        
        for (double v : values) mean += v;
        mean /= values.length;
        
        for (double v : values) {
            variance += Math.pow(v - mean, 2);
        }
        variance /= values.length;
        variance = Math.sqrt(variance);
        
        if (variance < 0.01) return 90.0;
        if (variance < 0.05) return 70.0;
        
        return 0.0;
    }
    
    private double detectZeroJitter(double[] values) {
        if (values.length < 5) return 0.0;
        
        long zeroCount = 0;
        for (double v : values) {
            if (v == 0.0) zeroCount++;
        }
        
        double zeroRatio = (double) zeroCount / values.length;
        
        if (zeroRatio > 0.8) return 88.0;
        if (zeroRatio > 0.5) return 65.0;
        
        return 0.0;
    }
    
    private double detectInstantReaction(double[] values) {
        if (values.length < 5) return 0.0;
        
        int instantCount = 0;
        for (double v : values) {
            if (v < 50) instantCount++;
        }
        
        double ratio = (double) instantCount / values.length;
        
        if (ratio > 0.7) return 92.0;
        if (ratio > 0.5) return 75.0;
        
        return 0.0;
    }
    
    private double detectPerfectPrediction(double[] values) {
        if (values.length < 5) return 0.0;
        
        int correctPredictions = 0;
        for (int i = 1; i < values.length; i++) {
            if (Math.abs(values[i] - values[i-1]) < 0.01) {
                correctPredictions++;
            }
        }
        
        double ratio = (double) correctPredictions / (values.length - 1);
        
        if (ratio > 0.9) return 94.0;
        if (ratio > 0.7) return 80.0;
        
        return 0.0;
    }
    
    public void removePlayer(Player player) {
        profiles.remove(player);
    }
    
    static class PatternProfile {
        private final Player player;
        private final Map<String, Deque<double[]>> patterns;
        
        PatternProfile(Player player) {
            this.player = player;
            this.patterns = new ConcurrentHashMap<>();
        }
        
        void recordPattern(String patternType, double[] values) {
            Deque<double[]> patternHistory = patterns.computeIfAbsent(patternType, k -> new ArrayDeque<>(50));
            patternHistory.addLast(values);
            if (patternHistory.size() > 50) patternHistory.removeFirst();
        }
        
        Deque<double[]> getPatternHistory(String patternType) {
            return patterns.getOrDefault(patternType, new ArrayDeque<>());
        }
    }
}
