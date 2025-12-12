package com.aetherguard.checks.combat;
import com.aetherguard.checks.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
public class KillAuraK extends CombatCheck {
    public KillAuraK(AetherGuard plugin, String category, String name, String type) { super(plugin, category, name, type); }
    @Override public CheckType getCheckType() { return CheckType.COMBAT_KILLAURA; }
    @Override public CheckResult check(Player player, CheckData data) { return CheckResult.pass(); }
}
