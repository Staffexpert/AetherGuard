package com.aetherguard.environment;

import org.bukkit.entity.Player;
import org.bukkit.Material;

/**
 * 🛡️ Environmental Hazard Analyzer
 * Exclusive Feature - Analyzes damage from environment (fire, fall, suffocation)
 */
public class EnvironmentalHazardAnalyzer {
    
    public double analyzeFallDamage(Player player, double distance) {
        double expected = (distance - 3) * 0.5;
        double maxHealth = player.getMaxHealth();
        double damage = player.getHealth();
        
        if (damage > maxHealth) return 90.0;
        
        return 0.0;
    }
    
    public double analyzeFireDamage(Player player, int fireTicks) {
        if (fireTicks < 0) return 80.0;
        return 0.0;
    }
    
    public double analyzeSuffocationDamage(Player player) {
        Material block = player.getLocation().getBlock().getType();
        
        if (block.isSolid()) return 50.0;
        
        return 0.0;
    }
}
