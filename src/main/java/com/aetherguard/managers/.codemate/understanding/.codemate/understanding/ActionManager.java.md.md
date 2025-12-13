The `ActionManager` class in the `com.aetherguard.managers` package manages and executes various player-related actions within the AetherGuard Minecraft plugin, primarily in response to player infractions detected by checks. Its core responsibilities include:

- **Action Handling:** Maintains a registry mapping action types (e.g., FLAG, ALERT, KICK, BAN, TEMPBAN, FREEZE, COMMAND, LOG, WEBHOOK) to their corresponding handler methods, enabling modular and extensible action execution.

- **Action Execution Workflow:** Provides functionality to parse action definitions, enforce cooldowns, handle optional execution delays, and support a test mode where actions are logged but not performed.

- **Action Parsing:** Converts raw action strings into structured `ActionInfo` objects encapsulating the action type, parameters, cooldown periods, and delays.

- **Specific Action Implementations:**
  - *FLAG:* Placeholder for flagging violations.
  - *ALERT:* Sends formatted alert messages to online staff members with appropriate permissions.
  - *KICK:* Disconnects the player with a specified or default message.
  - *BAN:* Applies a permanent ban with a reason.
  - *TEMPBAN:* Applies a temporary ban for a parsed duration, scheduling automatic unbanning.
  - *FREEZE:* Placeholder for freezing a player temporarily (default 30 seconds).
  - *COMMAND:* Executes server commands with dynamic placeholders replaced by player and check data.
  - *LOG:* Logs detailed action and player information to the console (with plans for file logging).
  - *WEBHOOK:* Placeholder for sending webhook notifications.

- **Duration Parsing:** Interprets duration strings with suffixes (seconds, minutes, hours, days) into seconds for use in timed actions like tempbans and freezes.

- **Concurrency and Cooldown Management:** Utilizes thread-safe data structures to track and enforce cooldowns on actions per player, preventing rapid repeated executions.

Overall, `ActionManager` centralizes and orchestrates the plugin’s response mechanisms to player infractions, facilitating configurable, delayed, and permission-aware punishments and notifications within the Minecraft server environment.