package com.aetherguard.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 🛡️ Inventory Analyzer
 * Exclusive Feature - Detects impossible items in inventory
 */
public class InventoryAnalyzer {
    
    public double analyzeInventory(Player player) {
        double suspicion = 0.0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            if (item.getAmount() > item.getMaxStackSize()) {
                suspicion += 95.0;
            }
        }
        
        return Math.min(suspicion, 100.0);
    }
}
