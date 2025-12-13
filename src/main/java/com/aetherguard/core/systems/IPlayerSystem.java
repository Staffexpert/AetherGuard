package com.aetherguard.core.systems;

import com.aetherguard.managers.PlayerManager.PlayerData;
import org.bukkit.entity.Player;

public interface IPlayerSystem {
    void registerPlayer(Player player);

    void unregisterPlayer(Player player);

    PlayerData getPlayerData(Player player);

    void savePlayerData(Player player);

    void saveAllData();

    boolean isExempt(Player player);

    boolean isExemptFromCheck(Player player, String checkName);

    void setExempt(Player player, boolean exempt);

    void setCheckExemption(Player player, String checkName, boolean exempt);
}
