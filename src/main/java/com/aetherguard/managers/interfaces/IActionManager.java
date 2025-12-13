package com.aetherguard.managers.interfaces;

import org.bukkit.entity.Player;

public interface IActionManager {
    void executeAction(Player player, String action, Object source, Object result);
    void executeActionAsync(Player player, String action, Object source, Object result);
    boolean isActionCooldownActive(Player player, String action);
    void setActionCooldown(Player player, String action, long durationMs);
    int getExecutedActionsCount();
    void resetActionCooldowns(Player player);
}