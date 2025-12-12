package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard KillAura Type D Check
 *
 * Line of sight validation
 * Detects when players attack targets they cannot see (through walls)
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class KillAuraD extends CombatCheck {

    private final double maxAngleTolerance;

    public KillAuraD(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.maxAngleTolerance = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.D.max-angle-tolerance", 45.0);
    }

    @Override
    public CheckType getCheckType() {
        return CheckType.COMBAT_KILLAURA;
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

        // Check line of sight
        if (!hasLineOfSight(player, combat.target)) {
            return CheckResult.violation("Attack through wall detected",
                String.format("Distance: %.2f, Has wall: %b", combat.reach, hasWallBetween(player, combat.target)));
        }

        // Check if player is looking at target
        if (!isLookingAt(player, combat.target, maxAngleTolerance)) {
            return CheckResult.violation("Attack without looking detected",
                String.format("Angle: %.2f°, Max allowed: %.2f°", combat.angle, maxAngleTolerance));
        }

        return CheckResult.pass();
    }
}
