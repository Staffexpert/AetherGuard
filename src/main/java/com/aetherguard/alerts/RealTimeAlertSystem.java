package com.aetherguard.alerts;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Real-Time Alert System
 * Exclusive Feature - Instant notifications for critical detections
 */
public class RealTimeAlertSystem {
    
    private final Map<String, AlertRule> rules;
    
    public RealTimeAlertSystem() {
        this.rules = new ConcurrentHashMap<>();
        initializeDefaultRules();
    }
    
    private void initializeDefaultRules() {
        rules.put("KILLAURA", new AlertRule("KILLAURA", 80.0));
        rules.put("REACH", new AlertRule("REACH", 85.0));
        rules.put("FLY", new AlertRule("FLY", 90.0));
    }
    
    public void checkAndAlert(Player player, String checkName, double suspicion) {
        AlertRule rule = rules.get(checkName);
        
        if (rule != null && suspicion >= rule.threshold) {
            broadcastAlert(player, checkName, suspicion);
        }
    }
    
    private void broadcastAlert(Player player, String checkName, double suspicion) {
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("aetherguard.alerts")) {
                admin.sendMessage(String.format(
                    "§c[AC Alert] §f%s detected §c%s §f(%.0f%%)",
                    player.getName(), checkName, suspicion
                ));
            }
        }
    }
    
    static class AlertRule {
        String checkName;
        double threshold;
        
        AlertRule(String checkName, double threshold) {
            this.checkName = checkName;
            this.threshold = threshold;
        }
    }
}
