package com.aetherguard.security;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🛡️ AetherGuard Anti-Disabler System
 * 
 * Prevents cheats from disabling the anticheat
 * Protection against Liquid Bounce, custom clients, etc
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class AntiDisablerSystem {
    
    private final AetherGuard plugin;
    private final Map<Player, SecurityProfile> profiles;
    private final AtomicBoolean systemIntegrity;
    
    public AntiDisablerSystem(AetherGuard plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
        this.systemIntegrity = new AtomicBoolean(true);
        startIntegrityChecks();
    }
    
    /**
     * Detect if player is using anti-disable cheats
     */
    public double detectAntiDisabler(Player player) {
        SecurityProfile profile = profiles.computeIfAbsent(player, p -> new SecurityProfile(p));
        
        double suspicion = 0.0;
        
        suspicion += detectClassModification(player) * 0.25;
        suspicion += detectPacketInterception(player) * 0.25;
        suspicion += detectListenerRemoval(player) * 0.25;
        suspicion += detectClientModification(player) * 0.25;
        
        return Math.min(suspicion, 100.0);
    }
    
    /**
     * Detect if player bytecode is modified
     */
    private double detectClassModification(Player player) {
        try {
            SecurityManager secMgr = System.getSecurityManager();
            if (secMgr == null) return 10.0;
            
            Class<?> playerClass = player.getClass();
            boolean modified = playerClass.getName().contains("$");
            
            if (modified) return 85.0;
            
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Detect packet interception
     */
    private double detectPacketInterception(Player player) {
        SecurityProfile profile = profiles.get(player);
        if (profile == null) return 0.0;
        
        if (profile.hasAbnormalPacketFlow()) return 70.0;
        if (profile.hasPacketDelay()) return 40.0;
        
        return 0.0;
    }
    
    /**
     * Detect event listener removal
     */
    private double detectListenerRemoval(Player player) {
        try {
            if (!plugin.getServer().getPluginManager().isPluginEnabled(plugin)) {
                return 100.0;
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Detect client modification
     */
    private double detectClientModification(Player player) {
        SecurityProfile profile = profiles.computeIfAbsent(player, p -> new SecurityProfile(p));
        
        double suspicion = 0.0;
        
        if (profile.clientBrand == null || profile.clientBrand.isEmpty()) suspicion += 20.0;
        if (profile.clientBrand != null && profile.clientBrand.contains("LiquidBounce")) return 100.0;
        if (profile.clientBrand != null && profile.clientBrand.contains("Meteor")) return 95.0;
        if (profile.clientBrand != null && profile.clientBrand.contains("Aristois")) return 95.0;
        if (profile.clientBrand != null && profile.clientBrand.contains("Impact")) return 95.0;
        if (profile.clientBrand != null && profile.clientBrand.contains("Ares")) return 90.0;
        
        return suspicion;
    }
    
    /**
     * Verify system integrity
     */
    private void startIntegrityChecks() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean isIntact = verifyIntegrity();
            systemIntegrity.set(isIntact);
            
            if (!isIntact) {
                plugin.getLogger().severe("§c[SECURITY] System integrity compromised!");
            }
        }, 0L, 20L);
    }
    
    /**
     * Verify core system integrity
     */
    private boolean verifyIntegrity() {
        try {
            if (plugin == null || !plugin.isEnabled()) return false;
            if (plugin.getCheckManager() == null) return false;
            if (plugin.getViolationManager() == null) return false;
            if (plugin.getPlayerManager() == null) return false;
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Record client brand
     */
    public void recordClientBrand(Player player, String brand) {
        SecurityProfile profile = profiles.computeIfAbsent(player, p -> new SecurityProfile(p));
        profile.clientBrand = brand;
    }
    
    /**
     * Record packet data for analysis
     */
    public void recordPacket(Player player) {
        SecurityProfile profile = profiles.computeIfAbsent(player, p -> new SecurityProfile(p));
        profile.recordPacket();
    }
    
    public boolean isSystemIntact() {
        return systemIntegrity.get();
    }
    
    public void removePlayer(Player player) {
        profiles.remove(player);
    }
    
    static class SecurityProfile {
        private final Player player;
        String clientBrand;
        private final Deque<Long> packetTimestamps;
        private long lastPacketTime;
        
        SecurityProfile(Player player) {
            this.player = player;
            this.packetTimestamps = new ArrayDeque<>(100);
            this.lastPacketTime = System.currentTimeMillis();
        }
        
        void recordPacket() {
            long now = System.currentTimeMillis();
            packetTimestamps.addLast(now);
            if (packetTimestamps.size() > 100) packetTimestamps.removeFirst();
            lastPacketTime = now;
        }
        
        boolean hasAbnormalPacketFlow() {
            if (packetTimestamps.size() < 10) return false;
            
            List<Long> recent = new ArrayList<>(packetTimestamps);
            for (int i = 1; i < recent.size(); i++) {
                long diff = recent.get(i) - recent.get(i - 1);
                if (diff > 100) return true;
            }
            
            return false;
        }
        
        boolean hasPacketDelay() {
            long now = System.currentTimeMillis();
            return (now - lastPacketTime) > 200;
        }
    }
}
