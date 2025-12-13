package com.aetherguard.combat;

import org.bukkit.entity.Player;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * ComboDetector - Detects inhuman combat combos and automated chains
 * Version: v1.2.0
 *
 * <p>Uses time-windowed intervals, average CPS and precision to decide
 * whether a player produces inhuman combat patterns. Designed to be
 * conservative and to combine with other detectors.</p>
 */
public final class ComboDetector {

    private static final int WINDOW_SECONDS = 6;

    private final ConcurrentHashMap<UUID, Deque<Long>> hits = new ConcurrentHashMap<>();

    public void recordHit(Player player) {
        if (player == null) return;
        Deque<Long> dq = hits.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();
        dq.addFirst(now);
        while (dq.size() > 128) dq.removeLast();
    }

    /**
     * Compute a conservative suspicion score for combos
     */
    public double detectCombo(Player player) {
        if (player == null) return 0.0;
        Deque<Long> dq = hits.get(player.getUniqueId());
        if (dq == null || dq.isEmpty()) return 0.0;

        long cutoff = System.currentTimeMillis() - WINDOW_SECONDS * 1000L;
        int count = 0;
        long prev = -1;
        int chained = 0;
        for (Long t : dq) {
            if (t < cutoff) break;
            count++;
            if (prev != -1 && (prev - t) < 200) chained++;
            prev = t;
        }

        double cps = count / (double) WINDOW_SECONDS;
        double chainRatio = count == 0 ? 0 : chained / (double) count;

        double score = 0.0;
        if (cps > 12.0) score += 40.0;
        if (cps > 17.0) score += 30.0;
        if (chainRatio > 0.6) score += 30.0;

        return Math.max(0.0, Math.min(100.0, score));
    }

    public void removePlayer(Player player) {
        if (player == null) return;
        hits.remove(player.getUniqueId());
    }
}
