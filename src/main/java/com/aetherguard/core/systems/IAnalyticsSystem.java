package com.aetherguard.core.systems;

public interface IAnalyticsSystem {
    long getTPS();

    double getMemoryUsagePercentage();

    long getCheckExecutionTime(String checkName);

    void recordCheckExecution(String checkName, long executionTime);

    int getTotalViolationsRecorded();

    int getPlayersOnlineCount();

    String getHealthStatus();
}
