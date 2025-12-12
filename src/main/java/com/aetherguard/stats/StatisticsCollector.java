package com.aetherguard.stats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Statistics Collector
 * Exclusive Feature - Real-time statistics and analytics
 */
public class StatisticsCollector {
    
    private final Map<String, Integer> checkDetections;
    private final Map<String, Long> executionTimes;
    
    public StatisticsCollector() {
        this.checkDetections = new ConcurrentHashMap<>();
        this.executionTimes = new ConcurrentHashMap<>();
    }
    
    public void recordDetection(String checkName) {
        checkDetections.merge(checkName, 1, Integer::sum);
    }
    
    public void recordExecutionTime(String checkName, long time) {
        executionTimes.merge(checkName, time, Long::sum);
    }
    
    public Map<String, Integer> getDetections() {
        return new HashMap<>(checkDetections);
    }
    
    public double getAverageExecutionTime(String checkName) {
        Integer detections = checkDetections.get(checkName);
        Long totalTime = executionTimes.get(checkName);
        
        if (detections == null || totalTime == null || detections == 0) return 0.0;
        
        return totalTime / (double) detections;
    }
}
