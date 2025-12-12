package com.aetherguard.checks.automation;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 🛡️ AetherGuard FastBreak Type A Check
 *
 * Basic fast break detection
 * Detects when players break blocks faster than normal
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class FastBreakA extends AutomationCheck {

    private static final String FASTBREAK_A_BREAKS_KEY = "fastbreak_a_breaks";
    private final double speedMultiplierThreshold;
    private final int breakHistorySize;

    public FastBreakA(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.speedMultiplierThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.automation.fastbreak.A.speed-multiplier-threshold", 2.0);
        this.breakHistorySize = plugin.getConfigManager().getMainConfig().getInt("checks.automation.fastbreak.A.break-history-size", 10);
    }

    @Override
    public CheckType getCheckType() {
        return CheckType.AUTOMATION_FASTBREAK;
    }

    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }

        // Check if this is a block break event
        if (!data.isBlockInteract()) {
            return CheckResult.pass();
        }

        // Track block break
        trackBlockBreak(player, data.getBlockType(), System.currentTimeMillis());

        // Check for fast break
        if (isFastBreakDetected(player, data.getBlockType())) {
            double breakSpeed = getBreakSpeedMultiplier(player, data.getBlockType(), player.getInventory().getItemInMainHand());
            return CheckResult.violation("FastBreak detected",
                String.format("Break speed: %.1fx normal for %s", breakSpeed, data.getBlockType()));
        }

        return CheckResult.pass();
    }

    /**
     * Track block breaks for analysis
     */
    private void trackBlockBreak(Player player, String blockType, long breakTime) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);

        @SuppressWarnings("unchecked")
        Deque<BlockBreakData> breaks = (Deque<BlockBreakData>) playerData.getCustomData().computeIfAbsent(FASTBREAK_A_BREAKS_KEY, k -> new ArrayDeque<BlockBreakData>());

        breaks.add(new BlockBreakData(blockType, breakTime));

        // Keep only recent breaks
        if (breaks.size() > breakHistorySize) {
            breaks.poll(); // More efficient than remove(0) for list
        }
    }

    /**
     * Check if fast break is detected
     */
    private boolean isFastBreakDetected(Player player, String blockType) {
        // This check is more complex now, so we do it once and pass the result
        // The main `check` method will handle the violation logic.
        // For simplicity, we'll calculate the multiplier and check there.
        // This method can be simplified or removed if the logic is fully in `check`.
        double multiplier = getBreakSpeedMultiplier(player, blockType, player.getInventory().getItemInMainHand());
        return multiplier > speedMultiplierThreshold;
    }

    /**
     * Calculate break speed multiplier
     */
    private double getBreakSpeedMultiplier(Player player, String blockType, ItemStack tool) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);

        @SuppressWarnings("unchecked")
        Deque<BlockBreakData> breaks = (Deque<BlockBreakData>) playerData.getCustomData().get(FASTBREAK_A_BREAKS_KEY);

        if (breaks == null || breaks.size() < 2) {
            return 1.0;
        }

        List<BlockBreakData> filtered = breaks.stream()
                .filter(b -> Objects.equals(b.blockType, blockType))
                .toList();
        
        if (filtered.size() < 2) {
            return 1.0;
        }
        
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < filtered.size(); i++) {
            long interval = filtered.get(i).breakTime - filtered.get(i - 1).breakTime;
            intervals.add(interval);
        }

        if (intervals.isEmpty()) {
            return 1.0;
        }

        double avgInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(500.0);

        // Get base break time for this material (Minecraft standard is ~1 second per block)
        Material material = Material.matchMaterial(blockType);
        if (material == null) return 1.0;

        double expectedInterval = 1000.0;
        
        return expectedInterval / avgInterval;
    }

    /**
     * Block break data container
     */
    private static class BlockBreakData {
        public final String blockType;
        public final long breakTime;

        public BlockBreakData(String blockType, long breakTime) {
            this.blockType = blockType;
            this.breakTime = breakTime;
        }
    }
}
