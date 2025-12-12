package com.aetherguard.checks;

/**
 * 🛡️ AetherGuard Check Result
 * 
 * Represents the result of a check execution
 * Contains violation information and debug data
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class CheckResult {
    
    private final ResultType type;
    private final AlertType alertType;
    private final double confidence;
    private final String debugInfo;
    private final String reason;
    private final long timestamp;
    
    // Predefined results
    public static final CheckResult PASS = new CheckResult(ResultType.PASS, AlertType.NONE, 0.0, "No violation", "No reason");
    public static final CheckResult VIOLATION = new CheckResult(ResultType.VIOLATION, AlertType.WARNING, 1.0, "Generic violation", "Generic violation");
    public static final CheckResult HIGH_VIOLATION = new CheckResult(ResultType.VIOLATION, AlertType.HIGH, 2.0, "High confidence violation", "High confidence violation");
    
    /**
     * Create a new check result
     */
    public CheckResult(ResultType type, AlertType alertType, double confidence, String debugInfo, String reason) {
        this.type = type;
        this.alertType = alertType;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.debugInfo = debugInfo;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a pass result
     */
    public static CheckResult pass() {
        return PASS;
    }
    
    /**
     * Create a violation result
     */
    public static CheckResult violation(String reason) {
        return new CheckResult(ResultType.VIOLATION, AlertType.WARNING, 1.0, reason, reason);
    }
    
    /**
     * Create a violation result with debug info
     */
    public static CheckResult violation(String reason, String debugInfo) {
        return new CheckResult(ResultType.VIOLATION, AlertType.WARNING, 1.0, debugInfo, reason);
    }
    
    /**
     * Create a violation result with confidence
     */
    public static CheckResult violation(String reason, double confidence) {
        return new CheckResult(ResultType.VIOLATION, AlertType.WARNING, confidence, reason, reason);
    }
    
    /**
     * Create a violation result with all parameters
     */
    public static CheckResult violation(String reason, String debugInfo, double confidence) {
        return new CheckResult(ResultType.VIOLATION, AlertType.WARNING, confidence, debugInfo, reason);
    }
    
    /**
     * Create a high confidence violation result
     */
    public static CheckResult highViolation(String reason) {
        return new CheckResult(ResultType.VIOLATION, AlertType.HIGH, 2.0, reason, reason);
    }
    
    /**
     * Create a high confidence violation result with debug info
     */
    public static CheckResult highViolation(String reason, String debugInfo) {
        return new CheckResult(ResultType.VIOLATION, AlertType.HIGH, 2.0, debugInfo, reason);
    }
    
    /**
     * Create a low confidence violation result
     */
    public static CheckResult lowViolation(String reason) {
        return new CheckResult(ResultType.VIOLATION, AlertType.LOW, 0.5, reason, reason);
    }
    
    /**
     * Create a suspicious result
     */
    public static CheckResult suspicious(String reason) {
        return new CheckResult(ResultType.SUSPICIOUS, AlertType.INFO, 0.3, reason, reason);
    }
    
    /**
     * Create a result with custom alert type
     */
    public static CheckResult custom(ResultType type, AlertType alertType, String reason, String debugInfo, double confidence) {
        return new CheckResult(type, alertType, confidence, debugInfo, reason);
    }
    
    /**
     * Check if this is a violation
     */
    public boolean isViolation() {
        return type == ResultType.VIOLATION;
    }
    
    /**
     * Check if this is suspicious
     */
    public boolean isSuspicious() {
        return type == ResultType.SUSPICIOUS;
    }
    
    /**
     * Check if this is a pass
     */
    public boolean isPass() {
        return type == ResultType.PASS;
    }
    
    /**
     * Check if this is a critical alert
     */
    public boolean isCritical() {
        return alertType == AlertType.CRITICAL;
    }
    
    /**
     * Check if this is a high alert
     */
    public boolean isHigh() {
        return alertType == AlertType.HIGH;
    }
    
    /**
     * Check if this requires immediate action
     */
    public boolean requiresAction() {
        return confidence >= 1.0 || alertType == AlertType.CRITICAL || alertType == AlertType.HIGH;
    }
    
    // Getters
    public ResultType getType() { return type; }
    public AlertType getAlertType() { return alertType; }
    public double getConfidence() { return confidence; }
    public String getDebugInfo() { return debugInfo; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("CheckResult{type=%s, alertType=%s, confidence=%.2f, reason='%s'}",
                type, alertType, confidence, reason);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CheckResult that = (CheckResult) obj;
        return Double.compare(that.confidence, confidence) == 0 &&
               type == that.type &&
               alertType == that.alertType &&
               debugInfo.equals(that.debugInfo) &&
               reason.equals(that.reason);
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + alertType.hashCode();
        result = 31 * result + Double.hashCode(confidence);
        result = 31 * result + debugInfo.hashCode();
        result = 31 * result + reason.hashCode();
        return result;
    }
    
    /**
     * Result type enumeration
     */
    public enum ResultType {
        PASS("No violation detected"),
        VIOLATION("Violation detected"),
        SUSPICIOUS("Suspicious activity detected"),
        ERROR("Error occurred during check");
        
        private final String description;
        
        ResultType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Alert type enumeration
     */
    public enum AlertType {
        NONE("No alert"),
        INFO("Information"),
        LOW("Low priority"),
        WARNING("Warning"),
        HIGH("High priority"),
        CRITICAL("Critical");
        
        private final String description;
        
        AlertType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * Get color for alert type
         */
        public String getColor() {
            switch (this) {
                case NONE: return "§7";
                case INFO: return "§b";
                case LOW: return "§e";
                case WARNING: return "§6";
                case HIGH: return "§c";
                case CRITICAL: return "§4";
                default: return "§f";
            }
        }
    }
}