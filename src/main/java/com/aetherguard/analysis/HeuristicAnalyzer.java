package com.aetherguard.analysis;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Heuristic Analyzer
 * 
 * Advanced behavioral analysis using heuristic patterns
 * Similar to Grim's physics prediction and Vulcan's pattern detection
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class HeuristicAnalyzer {
    
    private final AetherGuard plugin;
    private final Map<Player, BehaviorProfile> profiles;
    
    public HeuristicAnalyzer(AetherGuard plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
    }
    
    /**
     * Analyze player behavior for anomalies
     */
    public double analyzePlayerBehavior(Player player) {
        BehaviorProfile profile = profiles.computeIfAbsent(player, p -> new BehaviorProfile(p));
        
        double suspicionScore = 0.0;
        
        suspicionScore += analyzeMovementPatterns(profile) * 0.25;
        suspicionScore += analyzeCombatPatterns(profile) * 0.25;
        suspicionScore += analyzeReactionTimes(profile) * 0.20;
        suspicionScore += analyzeRotationSmoothing(profile) * 0.15;
        suspicionScore += analyzeConsistency(profile) * 0.15;
        
        return Math.min(suspicionScore, 100.0);
    }
    
    private double analyzeMovementPatterns(BehaviorProfile profile) {
        if (profile.movementHistory.size() < 5) return 0.0;
        
        double suspicion = 0.0;
        double[] speeds = profile.getRecentSpeeds(10);
        
        double avgSpeed = Arrays.stream(speeds).average().orElse(0);
        double variance = calculateVariance(speeds, avgSpeed);
        
        if (variance < 5.0) suspicion += 25.0;
        if (avgSpeed > 0.4) suspicion += 15.0;
        
        return suspicion;
    }
    
    private double analyzeCombatPatterns(BehaviorProfile profile) {
        if (profile.attackHistory.size() < 5) return 0.0;
        
        double suspicion = 0.0;
        long[] intervals = profile.getRecentAttackIntervals(10);
        
        double avgInterval = Arrays.stream(intervals).average().orElse(0);
        double variance = calculateVariance(intervals, avgInterval);
        
        if (variance < 20.0) suspicion += 30.0;
        if (avgInterval < 150.0) suspicion += 20.0;
        
        return suspicion;
    }
    
    private double analyzeReactionTimes(BehaviorProfile profile) {
        if (profile.reactionTimes.size() < 5) return 0.0;
        
        double suspicion = 0.0;
        double avgReaction = profile.reactionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
        
        if (avgReaction < 50.0) suspicion += 40.0;
        if (avgReaction < 100.0) suspicion += 20.0;
        
        return suspicion;
    }
    
    private double analyzeRotationSmoothing(BehaviorProfile profile) {
        if (profile.rotationHistory.size() < 10) return 0.0;
        
        double[] yawDifferences = profile.getYawDifferences();
        double avgDiff = Arrays.stream(yawDifferences).average().orElse(0);
        
        double suspicion = 0.0;
        if (avgDiff < 0.5) suspicion += 35.0;
        if (avgDiff == 0.0) suspicion += 25.0;
        
        return suspicion;
    }
    
    private double analyzeConsistency(BehaviorProfile profile) {
        double suspicion = 0.0;
        
        if (profile.hasConsistentPattern()) suspicion += 30.0;
        if (profile.isPerfectlyTimed()) suspicion += 25.0;
        if (profile.hasNoPossibleHumanError()) suspicion += 20.0;
        
        return suspicion;
    }
    
    private double calculateVariance(double[] values, double mean) {
        if (values.length == 0) return 0.0;
        double variance = 0.0;
        for (double v : values) {
            variance += Math.pow(v - mean, 2);
        }
        return Math.sqrt(variance / values.length);
    }
    
    private double calculateVariance(long[] values, double mean) {
        if (values.length == 0) return 0.0;
        double variance = 0.0;
        for (long v : values) {
            variance += Math.pow(v - mean, 2);
        }
        return Math.sqrt(variance / values.length);
    }
    
    public void recordMovement(Player player, double x, double y, double z, double vx, double vy, double vz) {
        BehaviorProfile profile = profiles.computeIfAbsent(player, p -> new BehaviorProfile(p));
        profile.recordMovement(x, y, z, vx, vy, vz);
    }
    
    public void recordAttack(Player player) {
        BehaviorProfile profile = profiles.computeIfAbsent(player, p -> new BehaviorProfile(p));
        profile.recordAttack();
    }
    
    public void recordRotation(Player player, float yaw, float pitch) {
        BehaviorProfile profile = profiles.computeIfAbsent(player, p -> new BehaviorProfile(p));
        profile.recordRotation(yaw, pitch);
    }
    
    public void removePlayer(Player player) {
        profiles.remove(player);
    }
    
    /**
     * Player behavior profile
     */
    public static class BehaviorProfile {
        private final Player player;
        private final Deque<MovementRecord> movementHistory;
        private final Deque<Long> attackHistory;
        private final Deque<Long> reactionTimes;
        private final Deque<RotationRecord> rotationHistory;
        
        private long lastAttackTime;
        private long lastMovementTime;
        private float lastYaw;
        
        public BehaviorProfile(Player player) {
            this.player = player;
            this.movementHistory = new ArrayDeque<>(100);
            this.attackHistory = new ArrayDeque<>(50);
            this.reactionTimes = new ArrayDeque<>(50);
            this.rotationHistory = new ArrayDeque<>(100);
            this.lastAttackTime = 0;
            this.lastMovementTime = System.currentTimeMillis();
            this.lastYaw = player.getLocation().getYaw();
        }
        
        public void recordMovement(double x, double y, double z, double vx, double vy, double vz) {
            MovementRecord record = new MovementRecord(x, y, z, vx, vy, vz);
            movementHistory.addLast(record);
            if (movementHistory.size() > 100) movementHistory.removeFirst();
            lastMovementTime = System.currentTimeMillis();
        }
        
        public void recordAttack() {
            long now = System.currentTimeMillis();
            if (lastAttackTime > 0) {
                long interval = now - lastAttackTime;
                attackHistory.addLast(interval);
                if (attackHistory.size() > 50) attackHistory.removeFirst();
                
                reactionTimes.addLast(interval);
                if (reactionTimes.size() > 50) reactionTimes.removeFirst();
            }
            lastAttackTime = now;
        }
        
        public void recordRotation(float yaw, float pitch) {
            RotationRecord record = new RotationRecord(yaw, pitch);
            rotationHistory.addLast(record);
            if (rotationHistory.size() > 100) rotationHistory.removeFirst();
            lastYaw = yaw;
        }
        
        public double[] getRecentSpeeds(int count) {
            int size = Math.min(count, movementHistory.size());
            double[] speeds = new double[size];
            int i = 0;
            for (MovementRecord record : movementHistory) {
                if (i >= size) break;
                speeds[i++] = Math.sqrt(record.vx * record.vx + record.vy * record.vy + record.vz * record.vz);
            }
            return speeds;
        }
        
        public long[] getRecentAttackIntervals(int count) {
            int size = Math.min(count, attackHistory.size());
            long[] intervals = new long[size];
            int i = 0;
            for (Long interval : attackHistory) {
                if (i >= size) break;
                intervals[i++] = interval;
            }
            return intervals;
        }
        
        public double[] getYawDifferences() {
            if (rotationHistory.size() < 2) return new double[0];
            double[] diffs = new double[rotationHistory.size() - 1];
            Iterator<RotationRecord> it = rotationHistory.iterator();
            RotationRecord prev = it.next();
            int i = 0;
            while (it.hasNext()) {
                RotationRecord curr = it.next();
                diffs[i++] = Math.abs(curr.yaw - prev.yaw);
                prev = curr;
            }
            return diffs;
        }
        
        public boolean hasConsistentPattern() {
            return attackHistory.size() >= 10 && 
                   calculateVariance(getRecentAttackIntervals(10), 
                   Arrays.stream(getRecentAttackIntervals(10)).average().orElse(0)) < 10.0;
        }
        
        public boolean isPerfectlyTimed() {
            long[] intervals = getRecentAttackIntervals(10);
            if (intervals.length == 0) return false;
            Set<Long> unique = new HashSet<>();
            for (long interval : intervals) unique.add(interval);
            return unique.size() == 1;
        }
        
        public boolean hasNoPossibleHumanError() {
            return getYawDifferences().length > 0 && 
                   Arrays.stream(getYawDifferences()).allMatch(d -> d == 0.0);
        }
        
        private double calculateVariance(long[] values, double mean) {
            if (values.length == 0) return 0.0;
            double variance = 0.0;
            for (long v : values) variance += Math.pow(v - mean, 2);
            return Math.sqrt(variance / values.length);
        }
    }
    
    static class MovementRecord {
        double x, y, z, vx, vy, vz;
        long timestamp;
        
        MovementRecord(double x, double y, double z, double vx, double vy, double vz) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static class RotationRecord {
        float yaw, pitch;
        long timestamp;
        
        RotationRecord(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
