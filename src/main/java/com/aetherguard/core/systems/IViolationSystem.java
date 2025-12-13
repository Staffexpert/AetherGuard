package com.aetherguard.core.systems;

import org.bukkit.entity.Player;

public interface IViolationSystem {
    void addViolation(Player player, String checkName);

    void addViolation(Player player, String checkName, double severity);

    long getTotalViolations(Player player);

    long getViolationsForCheck(Player player, String checkName);

    void clearViolations(Player player);

    void clearViolations(Player player, String checkName);

    int getViolationLevel(Player player);
}
