The `ActionManager` class in the `com.aetherguard.managers` package is responsible for managing and executing various punishment-related actions within the AetherGuard Minecraft plugin. It supports handling different types of actions triggered by anti-cheat checks, such as alerts, kicks, bans, temporary bans, freezes, commands, logging, and webhooks.

Key features and responsibilities include:

- **Action Handlers Initialization:** Maintains a mapping of action types (e.g., FLAG, ALERT, KICK, BAN, TEMPBAN, FREEZE, COMMAND, LOG, WEBHOOK) to their respective handler methods.

- **Action Execution:** Provides a method to execute an action on a player, respecting cooldowns and optional delays before execution. It supports test mode where actions are logged but not executed.

- **Action Parsing:** Parses action strings into structured `ActionInfo` objects containing the action type, parameters, cooldown, and delay.

- **Individual Action Implementations:**
  - **FLAG:** Placeholder for flagging violations.
  - **ALERT:** Sends formatted alert messages to online staff members with appropriate permissions.
  - **KICK:** Kicks the player with a specified or default reason.
  - **BAN:** Permanently bans the player with a specified or default reason and kicks them.
  - **TEMPBAN:** Temporarily bans the player for a specified duration and reason, schedules automatic unban.
  - **FREEZE:** Placeholder for freezing a player for a duration (default 30 seconds).
  - **COMMAND:** Executes a server command with placeholders replaced by player and check information.
  - **LOG:** Logs detailed information about the action and violation; intended to be extended to write to log files.
  - **WEBHOOK:** Placeholder for sending webhook notifications.

- **Duration Parsing:** Converts duration strings with suffixes (s, m, h, d) into seconds for use in temporary bans and freezes.

- **Concurrency and Cooldown Management:** Uses thread-safe structures to track the last time an action was executed per player to enforce cooldowns.

Overall, `ActionManager` centralizes the logic for responding to anti-cheat detections by executing configurable punishment or notification actions with support for delays, cooldowns, and extensibility.