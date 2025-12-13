package com.aetherguard.core.systems;

import org.bukkit.entity.Player;

public interface IActionSystem {
    void notify(Player player, String checkName);

    void kick(Player player, String reason);

    void ban(Player player, String reason);

    void freeze(Player player);

    void unfreeze(Player player);

    boolean isFrozen(Player player);

    void executeAction(Player player, String actionType, String reason);
}
