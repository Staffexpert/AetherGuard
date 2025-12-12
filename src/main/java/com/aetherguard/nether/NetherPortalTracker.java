package com.aetherguard.nether;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Nether Portal Tracker
 * Exclusive Feature - Tracks suspicious nether portal usage
 */
public class NetherPortalTracker {
    
    private final Map<Player, PortalProfile> profiles;
    
    public NetherPortalTracker() {
        this.profiles = new ConcurrentHashMap<>();
    }
    
    public void recordPortalUse(Player player, Location from, Location to) {
        PortalProfile profile = profiles.computeIfAbsent(player, p -> new PortalProfile());
        profile.recordTeleport(from, to);
    }
    
    public double analyzePortalUsage(Player player) {
        PortalProfile profile = profiles.get(player);
        if (profile == null) return 0.0;
        
        if (profile.teleportCount > 100) return 55.0;
        
        return 0.0;
    }
    
    static class PortalProfile {
        int teleportCount = 0;
        
        void recordTeleport(Location from, Location to) {
            teleportCount++;
        }
    }
}
