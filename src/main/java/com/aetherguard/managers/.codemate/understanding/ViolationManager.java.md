The `ViolationManager` class is part of the AetherGuard anti-cheat system for managing player violations related to various checks. It tracks, updates, and handles violations, including decay over time and triggering punishments when thresholds are met.

Key responsibilities and features:

- **Violation Tracking:** Maintains a concurrent map of players (by UUID) to their violations per check, encapsulated in `ViolationData` objects that store violation counts, timestamps, and history of recent violations.

- **Violation History:** Keeps a capped list of `ViolationRecord` entries per player, recording detailed information about each violation event (timestamp, check name, reason, confidence).

- **Adding Violations:** When a violation is added for a player and check, it updates the violation count, records the event in history, and checks if the violation count exceeds the configured maximum. If so, it triggers the configured punishment action and resets the violation count for that check.

- **Violation Decay:** Periodically reduces violation counts for players who have not committed recent violations, based on a configurable decay rate, to allow violations to expire over time.

- **Querying Violations:** Provides methods to retrieve violation data per player, per check, total violations, and violation history.

- **Resetting Violations:** Supports resetting violations either for all checks of a player or for a specific check.

- **Statistics:** Offers aggregated statistics about the current violation state, including total players with violations, total violations, and number of active checks being tracked.

- **Resource Management:** Includes cleanup methods to clear all stored violation data and history.

Supporting inner classes:

- `ViolationData`: Holds violation count, last violation time, and recent violation timestamps for a specific check.

- `ViolationRecord`: Represents a single violation event with details for history tracking.

- `ViolationStats`: Aggregates overall statistics about violations across all players.

Overall, this class centralizes violation management, enabling the anti-cheat system to monitor player behavior, enforce punishments, and maintain historical data for analysis or auditing.