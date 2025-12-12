package com.aetherguard.potions;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * 🛡️ Potion Effect Analyzer
 * Exclusive Feature - Detects impossible potion effects
 */
public class PotionEffectAnalyzer {
    
    public double analyzePotionEffects(Player player) {
        double suspicion = 0.0;
        
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getAmplifier() > 5) suspicion += 30.0;
            if (effect.getDuration() > 1000000) suspicion += 40.0;
        }
        
        return Math.min(suspicion, 100.0);
    }
}
