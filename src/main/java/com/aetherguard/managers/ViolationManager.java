package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    public static class Violation {
        public final String playerName;
        public final String checkInfo;
        public final String details;
        public final String timestamp;

        public Violation(Player player, String checkInfo, String details) {
            this.playerName = player.getName();
            this.checkInfo = checkInfo;
            this.details = details;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    public static class ViolationData {
        public final String checkName;
        public final long violations;
        
        public ViolationData(String checkName, long violations) {
            this.checkName = checkName;
            this.violations = violations;
        }
        
        public long getViolations() {
            return violations;
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

    private final AetherGuard plugin;
    private final Deque<Violation> recentViolations;
    private final Map<Player, Map<String, Long>> playerViolations;
    private final int maxViolations;

    public ViolationManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.maxViolations = 1000;
        this.recentViolations = new ArrayDeque<>();
        this.playerViolations = new ConcurrentHashMap<>();
    }

    public void addViolation(Violation violation) {
        if (recentViolations.size() >= maxViolations) {
            recentViolations.pollLast();
        }
        recentViolations.offerFirst(violation);
    }

    public void addViolation(Player player, String checkName) {
        playerViolations.computeIfAbsent(player, k -> new ConcurrentHashMap<>())
            .merge(checkName, 1L, Long::sum);
    }

    public Deque<Violation> getRecentViolations() {
        return new ArrayDeque<>(recentViolations);
    }

    public long getTotalViolations(Player player) {
        return playerViolations.getOrDefault(player, new HashMap<>())
            .values().stream().mapToLong(Long::longValue).sum();
    }

    public ViolationData getViolations(Player player, String checkName) {
        Map<String, Long> violations = playerViolations.getOrDefault(player, new HashMap<>());
        long count = violations.getOrDefault(checkName, 0L);
        return new ViolationData(checkName, count);
    }

    public void resetViolations(Player player) {
        playerViolations.remove(player);
    }

    public void resetViolations(Player player, String checkName) {
        Map<String, Long> violations = playerViolations.get(player);
        if (violations != null) {
            violations.remove(checkName);
        }
    }

    public void cleanup() {
        recentViolations.clear();
        playerViolations.clear();
    }

    public void cleanupOldViolations() {
        if (playerViolations.size() > 10000) {
            playerViolations.entrySet().removeIf(e -> !e.getKey().isOnline());
        }
    }

    public ViolationStats getStats() {
        long total = playerViolations.values().stream()
            .flatMap(m -> m.values().stream())
            .mapToLong(Long::longValue).sum();
        
        Map<String, Long> stats = new HashMap<>();
        for (Map<String, Long> playerStats : playerViolations.values()) {
            for (Map.Entry<String, Long> entry : playerStats.entrySet()) {
                stats.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }
        return new ViolationStats(total, stats);
    }
}
