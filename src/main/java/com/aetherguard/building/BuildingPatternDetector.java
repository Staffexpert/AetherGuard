package com.aetherguard.building;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * BuildingPatternDetector - Detects scaffold, tower and nuker patterns
 * Version: v1.2.0
 *
 * <p>Time-windowed analysis of placement positions and vertical clustering to
 * determine automated building/bypasses while remaining resilient to
 * lag-induced bursts.</p>
 */
public final class BuildingPatternDetector {

    private static final int WINDOW_SECONDS = 6;

    private final ConcurrentHashMap<UUID, Deque<PlacedBlock>> recentPlacements = new ConcurrentHashMap<>();

    public void recordBlockPlaced(Player player, Location loc) {
        if (player == null || loc == null) return;
        Deque<PlacedBlock> dq = recentPlacements.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentLinkedDeque<>());
        dq.addFirst(new PlacedBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Instant.now()));
        while (dq.size() > 256) dq.removeLast();
    }

    /**
     * Returns a 0..100 suspicion score for building-related cheats.
     */
    public double detectBuildingHacks(Player player) {
        if (player == null) return 0.0;
        Deque<PlacedBlock> dq = recentPlacements.get(player.getUniqueId());
        if (dq == null || dq.isEmpty()) return 0.0;

        Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);
        int total = 0, verticalStacks = 0, horizontalSpread = 0;
        PlacedBlock prev = null;
        for (PlacedBlock p : dq) {
            if (p.timestamp.isBefore(cutoff)) break;
            total++;
            if (prev != null) {
                if (p.x == prev.x && p.z == prev.z && Math.abs(p.y - prev.y) >= 1) verticalStacks++;
                if (Math.abs(p.x - prev.x) + Math.abs(p.z - prev.z) > 3) horizontalSpread++;
            }
            prev = p;
        }

        double score = 0.0;
        // rapid placements in very short windows -> scaffold/nuker
        if (total >= 12) score += 30.0;
        if (total >= 25) score += 40.0;
        // tall vertical stacks -> tower scaffolding
        if (verticalStacks >= 6) score += 25.0;
        if (horizontalSpread > total * 0.6) score += 15.0;

        return Math.max(0.0, Math.min(100.0, score));
    }

    private static final class PlacedBlock {
        final int x, y, z;
        final Instant timestamp;
        PlacedBlock(int x, int y, int z, Instant t) { this.x = x; this.y = y; this.z = z; this.timestamp = t; }
    }
}
