package com.aetherguard.analysis;

import com.aetherguard.checks.CheckData;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Statistical Analyzer v1.0.0
 *
 * Basic statistical analysis for pattern detection in player behavior
 * Uses standard deviation, mean analysis, and simple clustering
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class StatisticalAnalyzer {

    private final Map<Player, PlayerStats> playerStats;

    // Thresholds for anomaly detection
    private static final double ROTATION_CONSISTENCY_THRESHOLD = 0.1;
    private static final double TIMING_VARIANCE_THRESHOLD = 50.0;
    private static final double ANOMALY_ZSCORE_THRESHOLD = 3.0;
    private static final int MIN_SAMPLES_FOR_ANALYSIS = 10;

    public StatisticalAnalyzer() {
        this.playerStats = new ConcurrentHashMap<>();
    }

    /**
     * Analyze rotation consistency in player movements
     * @param player The player to analyze
     * @return Consistency score (0.0 = perfectly consistent, higher = less consistent)
     */
    public double analyzeRotationConsistency(Player player) {
        PlayerStats stats = playerStats.computeIfAbsent(player, p -> new PlayerStats(p));

        if (stats.rotationAngles.size() < MIN_SAMPLES_FOR_ANALYSIS) {
            return 0.0;
        }

        double[] angles = stats.rotationAngles.stream().mapToDouble(Double::doubleValue).toArray();
        double mean = calculateMean(angles);
        double stdDev = calculateStandardDeviation(angles, mean);

        // Lower standard deviation indicates more consistent rotations
        return Math.max(0.0, stdDev - ROTATION_CONSISTENCY_THRESHOLD);
    }

    /**
     * Detect timing patterns in player actions
     * @param player The player to analyze
     * @return Pattern score (0.0 = no pattern, higher = stronger pattern)
     */
    public double detectTimingPatterns(Player player) {
        PlayerStats stats = playerStats.computeIfAbsent(player, p -> new PlayerStats(p));

        if (stats.actionTimings.size() < MIN_SAMPLES_FOR_ANALYSIS) {
            return 0.0;
        }

        long[] timings = stats.actionTimings.stream().mapToLong(Long::longValue).toArray();
        double mean = calculateMean(timings);
        double stdDev = calculateStandardDeviation(timings, mean);

        // Low variance indicates consistent timing (possible automation)
        if (stdDev < TIMING_VARIANCE_THRESHOLD) {
            return Math.max(0.0, TIMING_VARIANCE_THRESHOLD - stdDev) / TIMING_VARIANCE_THRESHOLD * 100.0;
        }

        return 0.0;
    }

    /**
     * Detect anomalies using statistical methods
     * @param player The player to analyze
     * @param data The check data to analyze
     * @return Anomaly score (0.0 = normal, higher = more anomalous)
     */
    public double detectAnomalies(Player player, CheckData data) {
        PlayerStats stats = playerStats.computeIfAbsent(player, p -> new PlayerStats(p));

        double anomalyScore = 0.0;

        // Analyze movement speed anomalies
        if (data.getDistance() > 0) {
            stats.movementSpeeds.add(data.getDistance());
            if (stats.movementSpeeds.size() >= MIN_SAMPLES_FOR_ANALYSIS) {
                double zScore = calculateZScore(data.getDistance(), stats.movementSpeeds);
                if (Math.abs(zScore) > ANOMALY_ZSCORE_THRESHOLD) {
                    anomalyScore += Math.abs(zScore) * 10.0;
                }
            }
        }

        // Analyze attack timing anomalies
        if (data.isAttacked()) {
            long currentTime = System.currentTimeMillis();
            if (stats.lastAttackTime > 0) {
                long interval = currentTime - stats.lastAttackTime;
                stats.attackIntervals.add(interval);
                if (stats.attackIntervals.size() >= MIN_SAMPLES_FOR_ANALYSIS) {
                    double zScore = calculateZScore(interval, stats.attackIntervals);
                    if (Math.abs(zScore) > ANOMALY_ZSCORE_THRESHOLD) {
                        anomalyScore += Math.abs(zScore) * 15.0;
                    }
                }
            }
            stats.lastAttackTime = currentTime;
        }

        return Math.min(anomalyScore, 100.0);
    }

    /**
     * Perform simple clustering on movement patterns
     * @param player The player to analyze
     * @return Clustering score (0.0 = random, higher = clustered patterns)
     */
    public double performSimpleClustering(Player player) {
        PlayerStats stats = playerStats.computeIfAbsent(player, p -> new PlayerStats(p));

        if (stats.movementVectors.size() < MIN_SAMPLES_FOR_ANALYSIS) {
            return 0.0;
        }

        // Simple k-means like clustering with k=2
        List<double[]> vectors = new ArrayList<>(stats.movementVectors);
        double[] centroid1 = vectors.get(0);
        double[] centroid2 = vectors.get(vectors.size() / 2);

        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            List<double[]> cluster1 = new ArrayList<>();
            List<double[]> cluster2 = new ArrayList<>();

            for (double[] vector : vectors) {
                double dist1 = euclideanDistance(vector, centroid1);
                double dist2 = euclideanDistance(vector, centroid2);

                if (dist1 < dist2) {
                    cluster1.add(vector);
                } else {
                    cluster2.add(vector);
                }
            }

            if (!cluster1.isEmpty()) {
                centroid1 = calculateCentroid(cluster1);
            }
            if (!cluster2.isEmpty()) {
                centroid2 = calculateCentroid(cluster2);
            }
        }

        // Calculate cluster tightness (lower intra-cluster distance = tighter clusters)
        double intraClusterDistance = 0.0;
        for (double[] vector : vectors) {
            double dist1 = euclideanDistance(vector, centroid1);
            double dist2 = euclideanDistance(vector, centroid2);
            intraClusterDistance += Math.min(dist1, dist2);
        }

        double avgIntraDistance = intraClusterDistance / vectors.size();

        // Lower average distance indicates tighter clusters (more patterned behavior)
        return Math.max(0.0, 1.0 - avgIntraDistance) * 100.0;
    }

    /**
     * Record player data for analysis
     * @param player The player
     * @param data The check data
     */
    public void recordData(Player player, CheckData data) {
        PlayerStats stats = playerStats.computeIfAbsent(player, p -> new PlayerStats(p));

        // Record rotation data
        if (data.getAngle() > 0) {
            stats.rotationAngles.add(data.getAngle());
            if (stats.rotationAngles.size() > 100) {
                stats.rotationAngles.removeFirst();
            }
        }

        // Record timing data
        stats.actionTimings.add(data.getTimestamp());
        if (stats.actionTimings.size() > 100) {
            stats.actionTimings.removeFirst();
        }

        // Record movement vectors
        if (data.getVelocity() != null) {
            double[] vector = {
                data.getVelocity().getX(),
                data.getVelocity().getY(),
                data.getVelocity().getZ()
            };
            stats.movementVectors.add(vector);
            if (stats.movementVectors.size() > 50) {
                stats.movementVectors.removeFirst();
            }
        }
    }

    /**
     * Remove player data when they leave
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        playerStats.remove(player);
    }

    // Statistical utility methods

    private double calculateMean(double[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }

    private double calculateMean(long[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }

    private double calculateStandardDeviation(double[] values, double mean) {
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double calculateStandardDeviation(long[] values, double mean) {
        double variance = Arrays.stream(values)
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double calculateZScore(double value, Deque<Double> values) {
        double[] array = values.stream().mapToDouble(Double::doubleValue).toArray();
        double mean = calculateMean(array);
        double stdDev = calculateStandardDeviation(array, mean);
        return stdDev > 0 ? (value - mean) / stdDev : 0.0;
    }

    private double calculateZScore(long value, Deque<Long> values) {
        long[] array = values.stream().mapToLong(Long::longValue).toArray();
        double mean = calculateMean(array);
        double stdDev = calculateStandardDeviation(array, mean);
        return stdDev > 0 ? (value - mean) / stdDev : 0.0;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    private double[] calculateCentroid(List<double[]> vectors) {
        if (vectors.isEmpty()) return new double[0];

        int dimensions = vectors.get(0).length;
        double[] centroid = new double[dimensions];

        for (double[] vector : vectors) {
            for (int i = 0; i < dimensions; i++) {
                centroid[i] += vector[i];
            }
        }

        for (int i = 0; i < dimensions; i++) {
            centroid[i] /= vectors.size();
        }

        return centroid;
    }

    /**
     * Player statistics container
     */
    private static class PlayerStats {
        private final Player player;
        private final Deque<Double> rotationAngles;
        private final Deque<Long> actionTimings;
        private final Deque<Double> movementSpeeds;
        private final Deque<Long> attackIntervals;
        private final Deque<double[]> movementVectors;

        private long lastAttackTime;

        public PlayerStats(Player player) {
            this.player = player;
            this.rotationAngles = new ArrayDeque<>();
            this.actionTimings = new ArrayDeque<>();
            this.movementSpeeds = new ArrayDeque<>();
            this.attackIntervals = new ArrayDeque<>();
            this.movementVectors = new ArrayDeque<>();
            this.lastAttackTime = 0;
        }
    }
}