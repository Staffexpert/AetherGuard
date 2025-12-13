package com.aetherguard.core.config;

public final class AppConfig {
    public static final String VERSION = "1.2.0";
    public static final String PLUGIN_NAME = "AetherGuard";

    public static final class Threading {
        public static final int ASYNC_THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        public static final int BLOCKING_THREAD_COUNT = 2;
        public static final String ASYNC_THREAD_NAME = "AG-Async";
        public static final String BLOCKING_THREAD_NAME = "AG-Blocking";
    }

    public static final class Monitoring {
        public static final long TPS_CHECK_INTERVAL = 5;
        public static final long MEMORY_CHECK_INTERVAL = 10;
        public static final long VIOLATION_CLEANUP_INTERVAL = 60;
        public static final long PLAYER_DATA_SAVE_INTERVAL = 300;
        public static final double CRITICAL_TPS_THRESHOLD = 12.0;
        public static final double CRITICAL_MEMORY_THRESHOLD = 90.0;
    }

    public static final class Updates {
        public static final String GITHUB_API_URL = "https://api.github.com/repos/Staffexpert/AetherGuard-AntiCheat/releases/latest";
        public static final int GITHUB_TIMEOUT_MS = 5000;
    }

    public static final class Violations {
        public static final long VIOLATION_CUTOFF_TIME = 24 * 60 * 60 * 1000L;
        public static final int MAX_RECENT_VIOLATIONS = 1000;
        public static final int MAX_PLAYER_VIOLATIONS = 10000;
    }

    public static final class Executors {
        public static final long SHUTDOWN_TIMEOUT_ASYNC = 10;
        public static final long SHUTDOWN_TIMEOUT_BLOCKING = 5;
    }

    public static final class Default {
        public static final boolean UPDATE_CHECKER_ENABLED = true;
        public static final boolean DEBUG_MODE = false;
        public static final boolean MACHINE_LEARNING_ENABLED = false;
    }

    private AppConfig() {
        throw new AssertionError("Cannot instantiate AppConfig");
    }
}
