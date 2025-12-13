The `CheckManager` class serves as the central controller for managing all anti-cheat checks within the AetherGuard plugin. It organizes checks into categories and subtypes, supporting over 100 distinct checks spanning areas like movement, combat, world interactions, packets, exploits, automation, heuristics, and optional machine learning.

Core responsibilities include:

- **Initialization**: Creates and loads all checks, including dynamic sets (e.g., packet checks labeled A-Z), and conditionally includes machine learning checks based on configuration.

- **Configuration Handling**: Interfaces with a `ConfigManager` to load and apply settings for each check and category, managing enabled states and execution priorities.

- **Data Organization**: Maintains a hierarchical structure mapping categories to checks and their types, providing retrieval methods by full name or components, and filtering by enabled status or category.

- **Enable/Disable Management**: Supports toggling individual checks or entire categories on or off dynamically.

- **Statistics Tracking**: Monitors violation counts (flags) per check and exposes these metrics for reporting or GUI display.

- **Execution Ordering**: Sorts checks according to category priorities to control the order in which checks run.

- **Runtime Reloading**: Allows reloading of configurations and categories without restarting, updating enabled states and priorities accordingly.

- **Metadata Exposure**: Provides a `CheckInfo` data structure encapsulating check details such as category, name, type, enabled status, description, and violation counts for external use.

In summary, `CheckManager` acts as the authoritative registry and lifecycle manager for all anti-cheat checks, ensuring they are properly initialized, configured, prioritized, enabled/disabled, and monitored within the AetherGuard anti-cheat framework.