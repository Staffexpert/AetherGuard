package com.aetherguard.antispoof;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Spoof Detector
 * 
 * Detects position, rotation, and velocity spoofing
 * Similar to Verus' spoof detection
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class SpoofDetector {
    
    private final AetherGuard plugin;
    private final Map<Player, SpoofProfile> profiles;
    
    public SpoofDetector(AetherGuard plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
    }
    
    /**
     * Check for position spoofing
     */
    public double checkPositionSpoof(Player player, double x, double y, double z, double lastX, double lastY, double lastZ) {
        SpoofProfile profile = profiles.computeIfAbsent(player, p -> new SpoofProfile(p));
        
        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        profile.recordPosition(x, y, z, distance);
        
        double suspicion = 0.0;
        
        if (distance > 10.0) suspicion += 50.0;
        if (dx == 0 && dy == 0 && dz == 0 && !isIdle(player)) suspicion += 20.0;
        if (profile.hasInconsistentJumps()) suspicion += 30.0;
        
        return Math.min(suspicion, 100.0);
    }
    
    /**
     * Check for rotation spoofing
     */
    public double checkRotationSpoof(Player player, float yaw, float pitch, float lastYaw, float lastPitch) {
        SpoofProfile profile = profiles.computeIfAbsent(player, p -> new SpoofProfile(p));
        
        float yawDiff = normalizeAngle(yaw - lastYaw);
        float pitchDiff = Math.abs(pitch - lastPitch);
        
        profile.recordRotation(yaw, pitch, yawDiff, pitchDiff);
        
        double suspicion = 0.0;
        
        if (yawDiff > 180) suspicion += 25.0;
        if (yawDiff == 0 && pitchDiff == 0 && !isAiming(player)) suspicion += 15.0;
        if (profile.hasPerfectRotation()) suspicion += 35.0;
        if (profile.hasUnrealisticYaw()) suspicion += 40.0;
        
        return Math.min(suspicion, 100.0);
    }
    
    /**
     * Check for velocity spoofing
     */
    public double checkVelocitySpoof(Player player, Vector velocity, Vector lastVelocity) {
        SpoofProfile profile = profiles.computeIfAbsent(player, p -> new SpoofProfile(p));
        
        profile.recordVelocity(velocity);
        
        double suspicion = 0.0;
        
        double speed = velocity.length();
        double lastSpeed = lastVelocity.length();
        
        if (speed > 1.0 && player.isOnGround()) suspicion += 25.0;
        if (Math.abs(speed - lastSpeed) > 0.5) suspicion += 15.0;
        if (profile.hasImpossibleVelocity()) suspicion += 50.0;
        
        return Math.min(suspicion, 100.0);
    }
    
    /**
     * Check for ground spoofing (claiming to be on ground when not)
     */
    public double checkGroundSpoof(Player player, boolean claimedGround, boolean actuallyOnGround) {
        SpoofProfile profile = profiles.computeIfAbsent(player, p -> new SpoofProfile(p));
        
        double suspicion = 0.0;
        
        if (claimedGround && !actuallyOnGround) {
            suspicion += 30.0;
            profile.groundSpoofCount++;
        }
        
        if (profile.groundSpoofCount > 5) suspicion += 20.0;
        
        return Math.min(suspicion, 100.0);
    }
    
    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
    
    private boolean isIdle(Player player) {
        return player.getVelocity().length() < 0.01;
    }
    
    private boolean isAiming(Player player) {
        return player.getVelocity().length() > 0.1;
    }
    
    public void removePlayer(Player player) {
        profiles.remove(player);
    }
    
    static class SpoofProfile {
        private final Player player;
        private final Deque<PositionRecord> positionHistory;
        private final Deque<RotationRecord> rotationHistory;
        private final Deque<Vector> velocityHistory;
        
        int groundSpoofCount;
        
        SpoofProfile(Player player) {
            this.player = player;
            this.positionHistory = new ArrayDeque<>(50);
            this.rotationHistory = new ArrayDeque<>(50);
            this.velocityHistory = new ArrayDeque<>(50);
            this.groundSpoofCount = 0;
        }
        
        void recordPosition(double x, double y, double z, double distance) {
            positionHistory.addLast(new PositionRecord(x, y, z, distance));
            if (positionHistory.size() > 50) positionHistory.removeFirst();
        }
        
        void recordRotation(float yaw, float pitch, float yawDiff, float pitchDiff) {
            rotationHistory.addLast(new RotationRecord(yaw, pitch, yawDiff, pitchDiff));
            if (rotationHistory.size() > 50) rotationHistory.removeFirst();
        }
        
        void recordVelocity(Vector velocity) {
            velocityHistory.addLast(velocity.clone());
            if (velocityHistory.size() > 50) velocityHistory.removeFirst();
        }
        
        boolean hasInconsistentJumps() {
            if (positionHistory.size() < 5) return false;
            
            List<Double> distances = new ArrayList<>();
            for (PositionRecord record : positionHistory) {
                distances.add(record.distance);
            }
            
            double avg = distances.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            long outliers = distances.stream().filter(d -> Math.abs(d - avg) > avg).count();
            
            return outliers > distances.size() * 0.3;
        }
        
        boolean hasPerfectRotation() {
            if (rotationHistory.size() < 10) return false;
            long perfectTicks = rotationHistory.stream().filter(r -> r.yawDiff == 0 && r.pitchDiff == 0).count();
            return perfectTicks > rotationHistory.size() * 0.5;
        }
        
        boolean hasUnrealisticYaw() {
            if (rotationHistory.size() < 5) return false;
            return rotationHistory.stream().anyMatch(r -> Math.abs(r.yawDiff) > 120);
        }
        
        boolean hasImpossibleVelocity() {
            if (velocityHistory.size() < 3) return false;
            long impossibleCount = velocityHistory.stream().filter(v -> v.length() > 2.0).count();
            return impossibleCount > 0;
        }
    }
    
    static class PositionRecord {
        double x, y, z;
        double distance;
        long timestamp;
        
        PositionRecord(double x, double y, double z, double distance) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.distance = distance;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static class RotationRecord {
        float yaw, pitch;
        float yawDiff, pitchDiff;
        long timestamp;
        
        RotationRecord(float yaw, float pitch, float yawDiff, float pitchDiff) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.yawDiff = yawDiff;
            this.pitchDiff = pitchDiff;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
