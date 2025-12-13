package com.aetherguard.managers.interfaces;

import com.aetherguard.managers.PlayerManager.PlayerData;
import org.bukkit.entity.Player;

public interface IPlayerManager {
    void registerPlayer(Player player);
    void unregisterPlayer(Player player);
    PlayerData getPlayerData(Player player);
    void savePlayerData(Player player);
    void saveAllData();
    boolean isExempt(Player player);
    boolean isExempt(Player player, String checkName);
    boolean isExemptFromCheck(Player player, String checkName);
    void setExempt(Player player, boolean exempt);
    void setCheckExemption(Player player, String checkName, boolean exempt);
    void addExemption(Player player);
    void removeExemption(Player player);
    void addExemption(Player player, String checkName);
    void removeExemption(Player player, String checkName);
    void cleanup();
}