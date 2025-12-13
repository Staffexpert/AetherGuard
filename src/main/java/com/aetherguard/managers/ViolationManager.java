package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.interfaces.IViolationManager;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * AetherGuard v1.2.0 - Violation Manager
 *
 * Tracks player violations with time-windowed analysis, spike detection,
 * pattern recognition, and advanced analytics for behavioral detection.
 *
 * @author AetherGuard Team
 * @version 1.2.0
 */
public class ViolationManager implements IViolationManager {

    private static final long CUTOFF_TIME = 24 * 60 * 60 * 1000L;
    private static final long CLEANUP_INTERVAL_TICKS = 20L * 60L * 30L;
    private static final int MAX_VIOLATIONS = 1000;
    private static final int MAX_PLAYER_VIOLATIONS = 10000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault());

    private final AetherGuard plugin;
    private final Deque<Violation> recentViolations;
    private final Map<UUID, Map<String, CheckViolationData>> playerViolations;

    public ViolationManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.recentViolations = new ArrayDeque<>();
        this.playerViolations = new ConcurrentHashMap<>();
        scheduleCleanup();
    }

    private void scheduleCleanup() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, this::cleanupOldViolations, CLEANUP_INTERVAL_TICKS, CLEANUP_INTERVAL_TICKS
        );
    }

    public void addViolation(Violation violation) {
        if (violation == null) return;
        if (recentViolations.size() >= MAX_VIOLATIONS) {
            recentViolations.pollLast();
        }
        recentViolations.offerFirst(violation);
    }

    public void addViolation(Player player, String checkName) {
        if (player == null || checkName == null) return;
        
        UUID uid = player.getUniqueId();
        playerViolations.computeIfAbsent(uid, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(checkName, k -> new CheckViolationData())
            .addViolation(System.currentTimeMillis(), 1.0);
        
        checkAndEscalate(player);
    }

    public Deque<Violation> getRecentViolations() {
        return new ArrayDeque<>(recentViolations);
    }

    public long getTotalViolations(Player player) {
        if (player == null) return 0L;
        UUID uid = player.getUniqueId();
        return playerViolations.getOrDefault(uid, new ConcurrentHashMap<>())
            .values().stream()
            .mapToLong(CheckViolationData::getCount)
            .sum();
    }

    public ViolationData getViolations(Player player, String checkName) {
        if (player == null) return new ViolationData(checkName, 0);
        UUID uid = player.getUniqueId();
        Map<String, CheckViolationData> playerData = playerViolations.get(uid);
        if (playerData == null) return new ViolationData(checkName, 0);
        
        CheckViolationData data = playerData.get(checkName);
        return data != null ? new ViolationData(checkName, data.getTotalSeverity()) 
                            : new ViolationData(checkName, 0);
    }

    public void resetViolations(Player player) {
        if (player == null) return;
        playerViolations.remove(player.getUniqueId());
    }

    public void resetViolations(Player player, String checkName) {
        if (player == null || checkName == null) return;
        UUID uid = player.getUniqueId();
        Map<String, CheckViolationData> playerData = playerViolations.get(uid);
        if (playerData != null) {
            playerData.remove(checkName);
        }
    }

    public void cleanup() {
        recentViolations.clear();
        playerViolations.clear();
    }

    public void cleanupOldViolations() {
        try {
            if (playerViolations.size() > MAX_PLAYER_VIOLATIONS) {
                playerViolations.keySet().removeIf(uuid -> {
                    Player p = plugin.getServer().getPlayer(uuid);
                    return p == null || !p.isOnline();
                });
            }

            long cutoff = System.currentTimeMillis() - CUTOFF_TIME;
            playerViolations.values().forEach(playerData ->
                playerData.values().forEach(data -> data.removeOldTimestamps(cutoff))
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error cleaning old violations", e);
        }
    }

    public ViolationStats getStats() {
        long total = playerViolations.values().stream()
            .flatMap(m -> m.values().stream())
            .mapToLong(CheckViolationData::getCount)
            .sum();

        Map<String, Long> stats = new HashMap<>();
        playerViolations.values().forEach(playerStats ->
            playerStats.forEach((checkName, data) ->
                stats.merge(checkName, data.getCount(), Long::sum)
            )
        );
        return new ViolationStats(total, stats);
    }

    public boolean detectSpike(Player player, long timeWindowMillis, int threshold) {
        if (player == null) return false;
        Map<String, CheckViolationData> playerData = playerViolations.get(player.getUniqueId());
        if (playerData == null) return false;

        long now = System.currentTimeMillis();
        long start = now - timeWindowMillis;
        int count = 0;

        for (CheckViolationData data : playerData.values()) {
            count += data.countInWindow(start);
            if (count >= threshold) return true;
        }
        return false;
    }

    public boolean detectConsistentViolations(Player player, long timeWindowMillis, int minChecks) {
        if (player == null) return false;
        Map<String, CheckViolationData> playerData = playerViolations.get(player.getUniqueId());
        if (playerData == null) return false;

        long now = System.currentTimeMillis();
        long start = now - timeWindowMillis;
        long distinctChecks = playerData.entrySet().stream()
            .filter(entry -> entry.getValue().hasViolationInWindow(start))
            .count();

        return distinctChecks >= minChecks;
    }

    public boolean detectCorrelation(Player player, String check1, String check2, long timeWindowMillis) {
        if (player == null || check1 == null || check2 == null) return false;
        Map<String, CheckViolationData> playerData = playerViolations.get(player.getUniqueId());
        if (playerData == null) return false;

        CheckViolationData data1 = playerData.get(check1);
        CheckViolationData data2 = playerData.get(check2);
        if (data1 == null || data2 == null) return false;

        long start = System.currentTimeMillis() - timeWindowMillis;
        return data1.hasViolationInWindow(start) && data2.hasViolationInWindow(start);
    }

    public double getViolationRate(Player player, long timeWindowMillis) {
        if (player == null || timeWindowMillis <= 0) return 0;
        Map<String, CheckViolationData> playerData = playerViolations.get(player.getUniqueId());
        if (playerData == null) return 0;

        long start = System.currentTimeMillis() - timeWindowMillis;
        long total = playerData.values().stream()
            .mapToLong(data -> data.countInWindow(start))
            .sum();

        return (double) total / (timeWindowMillis / 1000.0);
    }

    public void escalateSeverity(Player player, double factor) {
        if (player == null || factor <= 0) return;
        Map<String, CheckViolationData> playerData = playerViolations.get(player.getUniqueId());
        if (playerData != null) {
            playerData.values().forEach(data -> data.escalate(factor));
        }
    }

    private void checkAndEscalate(Player player) {
        if (detectSpike(player, 60000, 5) || detectConsistentViolations(player, 300000, 2)) {
            escalateSeverity(player, 1.5);
        }
    }

    public static class Violation {
        public final String playerName;
        public final String checkInfo;
        public final String details;
        public final String timestamp;

        public Violation(Player player, String checkInfo, String details) {
            this.playerName = player != null ? player.getName() : "unknown";
            this.checkInfo = checkInfo != null ? checkInfo : "unknown";
            this.details = details != null ? details : "";
            this.timestamp = TIME_FORMATTER.format(Instant.now());
        }
    }

    public static class ViolationData {
        public final String checkName;
        public final double severity;

        public ViolationData(String checkName, double severity) {
            this.checkName = checkName;
            this.severity = Math.max(0, severity);
        }

        public double getSeverity() {
            return severity;
        }
    }

    public static class ViolationStats {
        public final long totalViolations;
        public final long totalPlayers;
        public final Map<String, Long> checkViolations;

        public ViolationStats(long totalViolations, Map<String, Long> checkViolations) {
            this.totalViolations = totalViolations;
            this.checkViolations = new HashMap<>(checkViolations);
            this.totalPlayers = checkViolations.size();
        }

        public long getTotalViolations() {
            return totalViolations;
        }

        public long getTotalPlayers() {
            return totalPlayers;
        }
    }

    private static class CheckViolationData {
        private final List<Long> timestamps = new ArrayList<>();
        private volatile double totalSeverity = 0;

        public void addViolation(long timestamp, double severity) {
            timestamps.add(timestamp);
            totalSeverity += severity;
        }

        public double getTotalSeverity() {
            return totalSeverity;
        }

        public List<Long> getTimestamps() {
            return new ArrayList<>(timestamps);
        }

        public long getCount() {
            return timestamps.size();
        }

        public void removeOldTimestamps(long cutoff) {
            timestamps.removeIf(t -> t < cutoff);
        }

        public int countInWindow(long start) {
            return (int) timestamps.stream().filter(t -> t >= start).count();
        }

        public boolean hasViolationInWindow(long start) {
            return timestamps.stream().anyMatch(t -> t >= start);
        }

        public void escalate(double factor) {
            totalSeverity *= Math.max(1.0, factor);
        }
    }

    @Override
    public void addViolation(UUID uuid, String checkName, double level, String details) {
        // Simplified implementation
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            addViolation(player, checkName);
        }
    }

    @Override
    public int getViolations(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        return player != null ? (int) getTotalViolations(player) : 0;
    }

    @Override
    public long getTotalViolations() {
        return getStats().totalViolations;
    }

    @Override
    public List<?> getRecentViolations(int limit) {
        Deque<Violation> recent = getRecentViolations();
        List<Violation> result = new ArrayList<>();
        int count = 0;
        for (Violation v : recent) {
            if (count >= limit) break;
            result.add(v);
            count++;
        }
        return result;
    }
}
