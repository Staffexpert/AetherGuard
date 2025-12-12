package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

public class KillAuraC extends CombatCheck {
    
    public KillAuraC(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        return CheckType.COMBAT_KILLAURA;
    }
    
    @Override
    public CheckResult check(Player player, CheckData data) {
        return CheckResult.pass();
    }
}
