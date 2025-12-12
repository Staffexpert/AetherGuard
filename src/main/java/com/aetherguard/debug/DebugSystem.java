package com.aetherguard.debug;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Debug System
 * 
 * Advanced debugging and monitoring system
 * Shows real-time check results and violations
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class DebugSystem {
    
    private final AetherGuard plugin;
    private final Map<Player, DebugSession> sessions;
    private boolean globalDebug;
    
    public DebugSystem(AetherGuard plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
        this.globalDebug = false;
    }
    
    /**
     * Enable debug mode for player
     */
    public void enableDebug(Player player) {
        sessions.put(player, new DebugSession(player));
        player.sendMessage("§a[AetherGuard] §fDebug mode enabled");
    }
    
    /**
     * Disable debug mode for player
     */
    public void disableDebug(Player player) {
        sessions.remove(player);
        player.sendMessage("§c[AetherGuard] §fDebug mode disabled");
    }
    
    /**
     * Log check result
     */
    public void logCheckResult(Player player, String checkName, boolean violated, String details) {
        DebugSession session = sessions.get(player);
        if (session != null) {
            session.addLog(checkName, violated, details);
            
            if (violated) {
                player.sendMessage(String.format("§c[%s] §f%s", checkName, details));
            }
        }
    }
    
    /**
     * Send debug info to player
     */
    public void sendDebugInfo(Player player) {
        DebugSession session = sessions.get(player);
        if (session != null) {
            player.sendMessage("§6=== AetherGuard Debug Info ===");
            player.sendMessage("§fPing: §c" + getPing(player) + "ms");
            player.sendMessage("§fTPS: §c" + getTPS());
            player.sendMessage("§fRecent Violations: §c" + session.getRecentViolations());
            player.sendMessage("§fSession Duration: §c" + session.getDuration() + "s");
        }
    }
    
    private double getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return ((Number) entityPlayer.getClass().getField("ping").get(entityPlayer)).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double getTPS() {
        try {
            Object tps = plugin.getServer().getClass().getMethod("getTPS").invoke(plugin.getServer());
            if (tps instanceof double[]) {
                return ((double[]) tps)[0];
            }
        } catch (Exception e) {
            // Fallback
        }
        return 20.0;
    }
    
    public boolean isDebugEnabled(Player player) {
        return sessions.containsKey(player);
    }
    
    public boolean isGlobalDebug() {
        return globalDebug;
    }
    
    public void setGlobalDebug(boolean enabled) {
        this.globalDebug = enabled;
    }
    
    static class DebugSession {
        private final Player player;
        private final Deque<DebugLog> logs;
        private final long startTime;
        
        DebugSession(Player player) {
            this.player = player;
            this.logs = new ArrayDeque<>(100);
            this.startTime = System.currentTimeMillis();
        }
        
        void addLog(String check, boolean violated, String details) {
            logs.addLast(new DebugLog(check, violated, details));
            if (logs.size() > 100) logs.removeFirst();
        }
        
        int getRecentViolations() {
            return (int) logs.stream().filter(l -> l.violated).count();
        }
        
        long getDuration() {
            return (System.currentTimeMillis() - startTime) / 1000;
        }
    }
    
    static class DebugLog {
        String check;
        boolean violated;
        String details;
        long timestamp;
        
        DebugLog(String check, boolean violated, String details) {
            this.check = check;
            this.violated = violated;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
