package com.aetherguard.hunger;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Hunger Analyzer
 * Exclusive Feature - Detects impossible hunger level changes
 */
public class HungerAnalyzer {
    
    private final Map<Player, HungerProfile> profiles;
    
    public HungerAnalyzer() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double analyzeHunger(Player player) {
        HungerProfile profile = profiles.computeIfAbsent(player, p -> new HungerProfile(p.getFoodLevel()));
        
        int currentHunger = player.getFoodLevel();
        int delta = currentHunger - profile.lastHunger;
        
        profile.recordHungerChange(delta);
        
        if (delta > 10) return 70.0;
        if (profile.hasInconsistentHunger()) return 50.0;
        
        return 0.0;
    }
    
    static class HungerProfile {
        int lastHunger;
        Deque<Integer> hungerHistory = new ArrayDeque<>();
        
        HungerProfile(int initialHunger) {
            this.lastHunger = initialHunger;
        }
        
        void recordHungerChange(int delta) {
            hungerHistory.addLast(delta);
            if (hungerHistory.size() > 100) hungerHistory.removeFirst();
        }
        
        boolean hasInconsistentHunger() {
            return hungerHistory.stream().filter(h -> h != 0).count() > 50;
        }
    }
}
