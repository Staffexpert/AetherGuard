package com.aetherguard.managers.interfaces;

import org.bukkit.entity.Player;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public interface IViolationManager {
    void addViolation(Player player, String checkName);
    void addViolation(UUID uuid, String checkName, double level, String details);
    int getViolations(UUID uuid);
    long getTotalViolations();
    long getTotalViolations(Player player);
    Deque<?> getRecentViolations();
    List<?> getRecentViolations(int limit);
    void resetViolations(Player player);
    void resetViolations(Player player, String checkName);
    void cleanupOldViolations();
    void cleanup();
    com.aetherguard.managers.ViolationManager.ViolationStats getStats();
}