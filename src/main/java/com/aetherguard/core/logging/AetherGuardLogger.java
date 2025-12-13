package com.aetherguard.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AetherGuardLogger {
    private final Logger delegate;
    private volatile boolean debugMode = false;

    public AetherGuardLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }

    public void info(String message) {
        delegate.log(Level.INFO, "§a[INFO] §f" + message);
    }

    public void warn(String message) {
        delegate.log(Level.WARNING, "§e[WARN] §f" + message);
    }

    public void error(String message) {
        delegate.log(Level.SEVERE, "§c[ERROR] §f" + message);
    }

    public void error(String message, Throwable throwable) {
        delegate.log(Level.SEVERE, "§c[ERROR] §f" + message, throwable);
    }

    public void debug(String message) {
        if (debugMode) {
            delegate.log(Level.INFO, "§d[DEBUG] §f" + message);
        }
    }

    public void success(String message) {
        delegate.log(Level.INFO, "§a✓ " + message);
    }

    public void critical(String message) {
        delegate.log(Level.SEVERE, "§c█ CRITICAL: " + message);
    }
}
