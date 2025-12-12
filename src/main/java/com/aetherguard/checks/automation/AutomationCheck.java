package com.aetherguard.checks.automation;

import com.aetherguard.checks.Check;
import com.aetherguard.core.AetherGuard;

/**
 * 🛡️ AetherGuard Automation Check Base Class
 * 
 * Base class for all automation-related checks (clicking, block breaking, etc)
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class AutomationCheck extends Check {
    
    protected static final double MAX_CPS = 20.0;
    protected static final double MAX_CPS_BURST = 25.0;
    protected static final long MIN_CLICK_INTERVAL = 50; // milliseconds
    
    public AutomationCheck(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    /**
     * Calculate clicks per second from click times
     */
    protected double calculateCPS(long[] clickTimes) {
        if (clickTimes == null || clickTimes.length == 0) {
            return 0.0;
        }
        
        long currentTime = System.currentTimeMillis();
        int clicksInLastSecond = 0;
        
        for (long clickTime : clickTimes) {
            if (currentTime - clickTime <= 1000) {
                clicksInLastSecond++;
            }
        }
        
        return clicksInLastSecond;
    }
    
    /**
     * Check if CPS is within acceptable range
     */
    protected boolean isAcceptableCPS(double cps, double maxCPS) {
        return cps <= maxCPS;
    }
    
    /**
     * Check if click pattern is human-like
     */
    protected boolean isHumanPattern(long[] clickTimes) {
        if (clickTimes == null || clickTimes.length < 3) {
            return true;
        }
        
        long[] intervals = new long[clickTimes.length - 1];
        for (int i = 1; i < clickTimes.length; i++) {
            intervals[i - 1] = clickTimes[i] - clickTimes[i - 1];
        }
        
        double variance = calculateVariance(intervals);
        return variance > 50.0;
    }
    
    /**
     * Calculate variance of intervals
     */
    protected double calculateVariance(long[] values) {
        if (values.length == 0) return 0.0;
        
        double mean = 0.0;
        for (long value : values) {
            mean += value;
        }
        mean /= values.length;
        
        double variance = 0.0;
        for (long value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        
        return Math.sqrt(variance);
    }
}
