The `ViolationManager` class in the AetherGuard plugin manages player violation tracking and enforcement within a Minecraft server. Its key responsibilities include:

- **Violation Tracking:** Maintains a thread-safe map linking player UUIDs to their violations, organized by check names. Each violation records details such as timestamp, reason, and confidence level.

- **Data Structures:** Utilizes nested classes to encapsulate violation information:
  - `ViolationData`: Tracks the count and timestamps of violations per check, supports adding new violations, decaying counts over time, and resetting.
  - `ViolationRecord`: Logs individual violation events with detailed metadata.
  - `ViolationStats`: Aggregates overall statistics like total players with violations, total violations, and active checks.

- **Violation Handling and Punishment:** Adds violations for players, and when counts exceed configured thresholds, triggers punishment actions and resets the violation count for that check.

- **Decay Mechanism:** Periodically reduces violation counts for inactive players/checks based on a configurable decay rate to avoid permanent penalties from old infractions.

- **History and Reporting:** Maintains a capped history of recent violations per player and provides methods to retrieve current violations, violation history, and aggregated statistics.

- **Reset and Cleanup:** Offers functionality to reset violations for specific players or checks and to clear all stored violation data for resource management.

In summary, `ViolationManager` acts as a centralized, scalable, and thread-safe system within AetherGuard to monitor, record, decay, and respond to player violations effectively.