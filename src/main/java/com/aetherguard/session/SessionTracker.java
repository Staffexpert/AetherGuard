package com.aetherguard.session;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Session Tracker
 * Exclusive Feature - Tracks suspicious session patterns
 */
public class SessionTracker {
    
    private final Map<UUID, SessionProfile> sessions;
    
    public SessionTracker() {
        this.sessions = new ConcurrentHashMap<>();
    }
    
    public void recordSessionStart(Player player) {
        sessions.put(player.getUniqueId(), new SessionProfile());
    }
    
    public void recordSessionEnd(Player player) {
        SessionProfile profile = sessions.get(player.getUniqueId());
        if (profile != null) {
            profile.recordEnd();
        }
    }
    
    public double analyzeSessionPattern(Player player) {
        SessionProfile profile = sessions.get(player.getUniqueId());
        if (profile == null) return 0.0;
        
        if (profile.sessionCount > 50) return 40.0;
        
        return 0.0;
    }
    
    static class SessionProfile {
        int sessionCount = 1;
        long startTime = System.currentTimeMillis();
        
        void recordEnd() {
            sessionCount++;
        }
    }
}
