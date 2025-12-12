package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard Reach Type A Check
 *
 * Basic reach validation
 * Validates attack distance with lag compensation
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ReachA extends CombatCheck {

    private final double reachTolerance;

    public ReachA(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.reachTolerance = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.reach.A.reach-tolerance", 0.1);
    }

    @Override
    public CheckType getCheckType() {
        return CheckType.COMBAT_REACH;
    }

    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }

        CombatData combat = getCombatData(data);

        // Check if target is valid
        if (!combat.isValidTarget()) {
            return CheckResult.pass();
        }

        // Calculate maximum allowed reach
        double maxReach = getMaxReachDistance(player);

        // Check if reach exceeds maximum
        if (combat.reach > maxReach + reachTolerance) {
            return CheckResult.violation("Reach violation detected",
                String.format("Reach: %.3f, Max: %.3f, Ping: %.0fms",
                    combat.reach, maxReach, getPing(player)));
        }

        return CheckResult.pass();
    }
}
