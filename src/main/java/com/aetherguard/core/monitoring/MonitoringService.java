package com.aetherguard.core.monitoring;

import com.aetherguard.core.config.AppConfig;
import com.aetherguard.core.logging.AetherGuardLogger;
import com.aetherguard.core.systems.IAnalyticsSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;

public class MonitoringService implements IAnalyticsSystem {
    private final AetherGuardLogger logger;
    private volatile long lastTPS = 20;
    private volatile double memoryUsagePercentage = 0.0;
    private final AtomicLong totalViolations = new AtomicLong(0);
    private final Map<String, Long> checkExecutionTimes = new HashMap<>();

    public MonitoringService(AetherGuardLogger logger) {
        this.logger = logger;
    }

    @Override
    public long getTPS() {
        return lastTPS;
    }

    @Override
    public double getMemoryUsagePercentage() {
        return memoryUsagePercentage;
    }

    @Override
    public long getCheckExecutionTime(String checkName) {
        return checkExecutionTimes.getOrDefault(checkName, 0L);
    }

    @Override
    public void recordCheckExecution(String checkName, long executionTime) {
        checkExecutionTimes.put(checkName, executionTime);
    }

    @Override
    public int getTotalViolationsRecorded() {
        return (int) totalViolations.get();
    }

    @Override
    public int getPlayersOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public String getHealthStatus() {
        StringBuilder status = new StringBuilder();
        status.append("TPS: ").append(lastTPS).append(" | ");
        status.append("Memory: ").append(String.format("%.1f%%", memoryUsagePercentage)).append(" | ");
        status.append("Players: ").append(getPlayersOnlineCount()).append(" | ");
        status.append("Violations: ").append(getTotalViolationsRecorded());
        return status.toString();
    }

    public void updateTPS() {
        try {
            Object tpsObj = Bukkit.getServer().getClass().getMethod("getTPS").invoke(Bukkit.getServer());
            if (tpsObj instanceof double[]) {
                double[] tps = (double[]) tpsObj;
                lastTPS = Math.round(tps[0]);

                if (tps[0] < AppConfig.Monitoring.CRITICAL_TPS_THRESHOLD) {
                    logger.warn(
                        "Critical TPS detected: " + String.format("%.2f", tps[0])
                    );
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to retrieve TPS: " + e.getMessage());
        }
    }

    public void updateMemory() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        memoryUsagePercentage = (double) used / runtime.maxMemory() * 100.0;

        if (memoryUsagePercentage > AppConfig.Monitoring.CRITICAL_MEMORY_THRESHOLD) {
            logger.warn(
                "Critical memory usage: " + String.format("%.1f%%", memoryUsagePercentage)
            );
        }
    }

    public void incrementViolations() {
        totalViolations.incrementAndGet();
    }
}
