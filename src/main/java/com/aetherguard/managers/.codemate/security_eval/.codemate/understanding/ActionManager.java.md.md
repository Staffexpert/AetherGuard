# ActionManager Class - High-Level Documentation

The `ActionManager` class is part of a Minecraft plugin responsible for managing and executing punishment-related actions on players based on detected checks and results. It supports various action types such as kicking, banning, temporary banning, freezing, and running server commands.

## Core Responsibilities

- **Action Parsing:** Converts action strings into structured action objects containing a type and parameters.
- **Action Execution:** Executes specified actions on players, invoking appropriate handlers for each action type.
- **Cooldown Management:** Tracks and enforces cooldown periods per player to prevent rapid repeated punishments.
- **Duration Parsing:** Parses human-readable duration strings (e.g., "10m", "2h") into time values for temporary bans.
- **Scheduling:** Schedules delayed tasks such as unbanning players after temporary ban durations expire.

## Supported Action Types

- **KICK:** Immediately disconnects a player with a specified reason.
- **BAN:** Permanently bans a player with a reason.
- **TEMPBAN:** Temporarily bans a player for a specified duration.
- **FREEZE:** Applies a freeze effect to a player.
- **COMMAND:** Executes a server command, potentially with placeholders replaced by player and check information.

## Key Components

- **Action Parsing (`parseAction`):** Splits action strings into type and parameter components without strict validation.
- **Action Handlers:** A mapping of action types to handler implementations that perform the actual punishment logic.
- **Cooldown Tracking:** Uses a concurrent map to record the last execution time of actions per player UUID, enforcing cooldown intervals.
- **Duration Parsing (`parseDuration`):** Converts duration strings with suffixes (seconds, minutes, hours, days) into numeric values.
- **Command Execution:** Constructs commands by replacing placeholders with dynamic data and dispatches them as console commands.

## Security and Operational Considerations

- **Input Validation:** Currently lacks strict validation of action types, parameters, and duration inputs.
- **Authorization:** Does not enforce permission checks before executing actions, potentially allowing unauthorized punishments.
- **Command Safety:** Executes dynamically constructed commands without sanitization, posing risks of command injection.
- **Concurrency:** Cooldown checks and updates are not atomic, which may lead to race conditions under concurrent access.
- **Duration Limits:** No upper bounds on temporary ban durations, risking scheduling issues or abuse.

## Summary

The `ActionManager` class orchestrates punishment actions triggered by plugin checks, handling parsing, execution, cooldowns, and scheduling. While functionally comprehensive, it requires enhancements in input validation, authorization enforcement, command sanitization, and concurrency control to ensure secure and reliable operation within the server environment.