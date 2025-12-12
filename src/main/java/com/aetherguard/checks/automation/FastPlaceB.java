package com.aetherguard.checks.automation;
import com.aetherguard.checks.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
public class FastPlaceB extends AutomationCheck {
    public FastPlaceB(AetherGuard plugin, String category, String name, String type) { super(plugin, category, name, type); }
    @Override public CheckType getCheckType() { return CheckType.AUTOMATION_FASTPLACE; }
    @Override public CheckResult check(Player player, CheckData data) { return CheckResult.pass(); }
}
