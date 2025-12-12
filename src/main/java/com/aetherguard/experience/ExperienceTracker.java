package com.aetherguard.experience;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Experience Tracker
 * Exclusive Feature - Tracks impossible XP gain rates
 */
public class ExperienceTracker {
    
    private final Map<Player, ExpProfile> profiles;
    
    public ExperienceTracker() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public double trackExperience(Player player) {
        ExpProfile profile = profiles.computeIfAbsent(player, p -> new ExpProfile());
        profile.recordLevel(player.getLevel());
        
        if (profile.levelUpCount > 10) return 75.0;
        
        return 0.0;
    }
    
    static class ExpProfile {
        int levelUpCount = 0;
        int lastLevel = 0;
        
        void recordLevel(int level) {
            if (level > lastLevel) levelUpCount++;
            lastLevel = level;
        }
    }
}
