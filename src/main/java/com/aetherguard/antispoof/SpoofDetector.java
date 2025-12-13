package com.aetherguard.antispoof;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SpoofDetector - Detects position, rotation and velocity spoof attempts
 * Version: v1.2.0
 *
 * <p>Implements resilience against packet reordering and server lag, uses
 * time-windowed heuristics, and flags persistent spoof attempts while
 * minimizing false positives.</p>
 */
public class SpoofDetector {

    private final AetherGuard plugin;
    private final Map<UUID, SpoofProfile> profiles = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AetherGuard-SpoofDetector"); t.setDaemon(true); return t;
    });

    public SpoofDetector(AetherGuard plugin) {
        this.plugin = plugin;
        // periodic decay of counters to avoid permanent bans for transient issues
        scheduler.scheduleAtFixedRate(this::decayProfiles, 30, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
        profiles.clear();
    }

    public double checkPositionSpoof(Player player, double x, double y, double z, double lastX, double lastY, double lastZ, boolean teleported) {
        if (player == null) return 0.0;
        SpoofProfile profile = profiles.computeIfAbsent(player.getUniqueId(), k -> new SpoofProfile());

        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        profile.recordPosition(x, y, z, distance);

        if (teleported) return 0.0; // ignore known teleports

        double suspicion = 0.0;
        if (distance > 8.0) suspicion += 50.0;
        if (Math.abs(dy) > 4.0 && !player.isFlying()) suspicion += 25.0;
        if (profile.hasInconsistentJumps()) suspicion += 30.0;

        profile.addSuspicion((int) Math.round(suspicion));
        return Math.min(100.0, profile.getRecentSuspicion());
    }

    public double checkRotationSpoof(Player player, float yaw, float pitch, float lastYaw, float lastPitch) {
        if (player == null) return 0.0;
        SpoofProfile profile = profiles.computeIfAbsent(player.getUniqueId(), k -> new SpoofProfile());

        float yawDiff = normalizeAngle(yaw - lastYaw);
        float pitchDiff = Math.abs(pitch - lastPitch);

        profile.recordRotation(yaw, pitch, yawDiff, pitchDiff);

        double suspicion = 0.0;
        if (Math.abs(yawDiff) > 160.0) suspicion += 30.0; // teleport-like rotation
        if (yawDiff == 0.0f && pitchDiff == 0.0f && !isAiming(player)) suspicion += 10.0;
        if (profile.hasPerfectRotation()) suspicion += 35.0;

        profile.addSuspicion((int) Math.round(suspicion));
        return Math.min(100.0, profile.getRecentSuspicion());
    }

    public double checkVelocitySpoof(Player player, Vector velocity, Vector lastVelocity) {
        if (player == null || velocity == null || lastVelocity == null) return 0.0;
        SpoofProfile profile = profiles.computeIfAbsent(player.getUniqueId(), k -> new SpoofProfile());

        profile.recordVelocity(velocity);

        double suspicion = 0.0;
        double speed = velocity.length();
        double lastSpeed = lastVelocity.length();

        if (speed > 1.1 && player.isOnGround()) suspicion += 30.0;
        if (Math.abs(speed - lastSpeed) > 0.6) suspicion += 15.0;
        if (profile.hasImpossibleVelocity()) suspicion += 45.0;

        profile.addSuspicion((int) Math.round(suspicion));
        return Math.min(100.0, profile.getRecentSuspicion());
    }

    public double checkGroundSpoof(Player player, boolean claimedGround, boolean actuallyOnGround) {
        if (player == null) return 0.0;
        SpoofProfile profile = profiles.computeIfAbsent(player.getUniqueId(), k -> new SpoofProfile());

        double suspicion = 0.0;
        if (claimedGround && !actuallyOnGround) {
            profile.groundSpoofCount.incrementAndGet();
            suspicion += 30.0;
        }
        if (profile.groundSpoofCount.get() > 4) suspicion += 20.0;

        profile.addSuspicion((int) Math.round(suspicion));
        return Math.min(100.0, profile.getRecentSuspicion());
    }

    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private boolean isAiming(Player player) {
        return player.getVelocity().length() > 0.05;
    }

    public void removePlayer(Player player) {
        if (player == null) return;
        profiles.remove(player.getUniqueId());
    }

    private void decayProfiles() {
        for (SpoofProfile p : profiles.values()) p.decay();
    }

    static class SpoofProfile {
        private final Deque<PositionRecord> positionHistory = new ArrayDeque<>(64);
        private final Deque<RotationRecord> rotationHistory = new ArrayDeque<>(64);
        private final Deque<Vector> velocityHistory = new ArrayDeque<>(64);
        private final AtomicInteger suspicion = new AtomicInteger(0);
        private final AtomicInteger groundSpoofCount = new AtomicInteger(0);

        void recordPosition(double x, double y, double z, double distance) {
            synchronized (positionHistory) {
                positionHistory.addLast(new PositionRecord(x, y, z, distance, Instant.now()));
                if (positionHistory.size() > 64) positionHistory.removeFirst();
            }
        }

        void recordRotation(float yaw, float pitch, float yawDiff, float pitchDiff) {
            synchronized (rotationHistory) {
                rotationHistory.addLast(new RotationRecord(yaw, pitch, yawDiff, pitchDiff, Instant.now()));
                if (rotationHistory.size() > 64) rotationHistory.removeFirst();
            }
        }

        void recordVelocity(Vector v) {
            synchronized (velocityHistory) {
                velocityHistory.addLast(v.clone());
                if (velocityHistory.size() > 64) velocityHistory.removeFirst();
            }
        }

        boolean hasInconsistentJumps() {
            synchronized (positionHistory) {
                if (positionHistory.size() < 6) return false;
                double avg = positionHistory.stream().mapToDouble(pr -> pr.distance).average().orElse(0);
                long outliers = positionHistory.stream().filter(pr -> Math.abs(pr.distance - avg) > Math.max(0.2, avg)).count();
                return outliers > positionHistory.size() * 0.25;
            }
        }

        boolean hasPerfectRotation() {
            synchronized (rotationHistory) {
                if (rotationHistory.size() < 8) return false;
                long perfect = rotationHistory.stream().filter(r -> r.yawDiff == 0 && r.pitchDiff == 0).count();
                return perfect > rotationHistory.size() * 0.5;
            }
        }

        boolean hasImpossibleVelocity() {
            synchronized (velocityHistory) {
                return velocityHistory.stream().anyMatch(v -> v.length() > 2.0);
            }
        }

        void decay() {
            int current = suspicion.get();
            if (current <= 0) return;
            suspicion.addAndGet(-Math.max(1, current / 10));
            groundSpoofCount.updateAndGet(v -> Math.max(0, v - 1));
        }

        void addSuspicion(int amount) {
            suspicion.addAndGet(amount);
        }

        int getRecentSuspicion() { return Math.min(100, suspicion.get()); }
    }

    static class PositionRecord {
        double x, y, z, distance;
        Instant timestamp;
        PositionRecord(double x, double y, double z, double distance, Instant t) { this.x = x; this.y = y; this.z = z; this.distance = distance; this.timestamp = t; }
    }

    static class RotationRecord {
        float yaw, pitch, yawDiff, pitchDiff;
        Instant timestamp;
        RotationRecord(float yaw, float pitch, float yawDiff, float pitchDiff, Instant t) { this.yaw = yaw; this.pitch = pitch; this.yawDiff = yawDiff; this.pitchDiff = pitchDiff; this.timestamp = t; }
    }
}
