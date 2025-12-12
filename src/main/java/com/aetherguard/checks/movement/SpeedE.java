package com.aetherguard.checks.movement;
import com.aetherguard.checks.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
public class SpeedE extends MovementCheck {
    public SpeedE(AetherGuard plugin, String category, String name, String type) { super(plugin, category, name, type); }
    @Override public CheckType getCheckType() { return CheckType.MOVEMENT_SPEED; }
    @Override public CheckResult check(Player player, CheckData data) { return CheckResult.pass(); }
}
