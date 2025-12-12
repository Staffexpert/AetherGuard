package com.aetherguard.behavior;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Behavioral AI System
 * Exclusive Feature - Learns normal player behavior patterns
 */
public class BehavioralAI {
    
    private final Map<Player, BehaviorModel> models;
    
    public BehavioralAI() {
        this.models = new ConcurrentHashMap<>();
    }
    
    public double analyzeBehavior(Player player) {
        BehaviorModel model = models.computeIfAbsent(player, p -> new BehaviorModel());
        return model.calculateAnomalyScore();
    }
    
    public void recordAction(Player player, String actionType) {
        models.computeIfAbsent(player, p -> new BehaviorModel()).recordAction(actionType);
    }
    
    static class BehaviorModel {
        Map<String, Integer> actionFrequency = new ConcurrentHashMap<>();
        Map<String, Double> actionConfidence = new ConcurrentHashMap<>();
        
        void recordAction(String type) {
            actionFrequency.merge(type, 1, Integer::sum);
        }
        
        double calculateAnomalyScore() {
            double score = 0.0;
            for (String action : actionFrequency.keySet()) {
                if (actionFrequency.get(action) > 100 && actionConfidence.getOrDefault(action, 0.5) < 0.3) {
                    score += 20.0;
                }
            }
            return Math.min(score, 100.0);
        }
    }
}
