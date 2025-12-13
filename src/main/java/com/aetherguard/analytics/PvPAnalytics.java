package com.aetherguard.analytics;

import org.bukkit.entity.Player;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.LongAdder;

/**
 * PvPAnalytics - Advanced PvP metrics collector and analyzer
 * Version: v1.2.0
 *
 * <p>Collects hit, damage and precision metrics with time-windows and
 * produces a robust, explainable suspicion score that prioritizes
 * low false-positive rates.</p>
 */
public final class PvPAnalytics {

    private static final long WINDOW_SECONDS = 30;

    private final Map<UUID, PlayerWindow> windows = new ConcurrentHashMap<>();

    public void recordHit(Player attacker, Player victim, double damage, double aimPrecision) {
        if (attacker == null) return;
        windows.computeIfAbsent(attacker.getUniqueId(), k -> new PlayerWindow()).record(damage, aimPrecision);
    }

    /**
     * Returns a 0..100 suspicion score for PvP-related anomalies for the given player.
     */
    public double analyzePlayer(Player player) {
        if (player == null) return 0.0;
        PlayerWindow w = windows.get(player.getUniqueId());
        if (w == null) return 0.0;
        return w.suspicionScore();
    }

    private static final class PlayerWindow {
        private final ConcurrentLinkedDeque<TimestampedHit> hits = new ConcurrentLinkedDeque<>();
        private final LongAdder hitsTotal = new LongAdder();

        void record(double damage, double precision) {
            Instant now = Instant.now();
            hits.addFirst(new TimestampedHit(now, damage, precision));
            hitsTotal.increment();
            // prune old
            Instant cutoff = now.minusSeconds(WINDOW_SECONDS);
            while (!hits.isEmpty() && hits.getLast().timestamp.isBefore(cutoff)) hits.removeLast();
        }

        double suspicionScore() {
            Instant now = Instant.now();
            Instant cutoff = now.minusSeconds(WINDOW_SECONDS);
            int count = 0;
            double avgDamage = 0.0;
            double avgPrecision = 0.0;
            for (TimestampedHit h : hits) {
                if (h.timestamp.isBefore(cutoff)) break;
                count++;
                avgDamage += h.damage;
                avgPrecision += h.precision;
            }
            if (count == 0) return 0.0;
            avgDamage /= count;
            avgPrecision /= count;

            // Heuristics: extremely high precision + consistent damage -> suspicious
            double precisionScore = Math.max(0.0, (avgPrecision - 0.85) / 0.15); // 0..1
            double damageConsistency = Math.tanh(Math.abs(avgDamage - 6.0) / 6.0); // example normalization
            double activityFactor = Math.tanh((count - 5) / 10.0);

            double score = 100.0 * (0.6 * precisionScore + 0.25 * damageConsistency + 0.15 * activityFactor);
            return Math.max(0.0, Math.min(100.0, score));
        }

        private record TimestampedHit(Instant timestamp, double damage, double precision) { }
    }
}

