# ViolationManager Class - High-Level Documentation

The `ViolationManager` class is responsible for managing player violation tracking, maintaining violation histories, and executing punishments within a Minecraft server plugin environment. Its primary functions include recording violations detected by various checks, storing these violations per player, and triggering appropriate punishment actions when thresholds are met.

## Core Responsibilities

- **Violation Tracking:**  
  Records violations for players based on results from anti-cheat checks. Each violation includes details such as the check name, reason, and confidence level.

- **Violation History Management:**  
  Maintains a per-player history of violation records, capped at a fixed size to limit memory usage.

- **Punishment Execution:**  
  Automatically triggers configured punishment actions when a player's violation count for a specific check reaches a defined threshold.

- **Data Access:**  
  Provides access to a player's violation history for potential display or administrative review.

## Key Components

- **Data Structures:**  
  Uses a concurrent map keyed by player UUIDs to store lists of violation records. Each list holds recent violations for that player.

- **Concurrency Considerations:**  
  While the map is thread-safe, the lists of violations are not inherently thread-safe, which may lead to concurrency issues.

- **Input Handling:**  
  Stores violation details directly from check results without sanitization, which may pose risks if inputs contain malicious content.

- **Security and Access Control:**  
  Does not enforce permission checks on access to violation histories, potentially exposing sensitive player data.

- **Resource Management:**  
  Limits the size of violation histories per player but does not implement persistence or cleanup for inactive players, which could lead to memory growth over time.

## Security and Stability Considerations

- **Concurrency:**  
  The use of non-thread-safe lists within a concurrent map can cause race conditions and data corruption when multiple threads modify violation histories simultaneously.

- **Input Validation:**  
  Lack of sanitization on violation details may lead to log injection or client-side display issues.

- **Punishment Trust:**  
  Direct execution of punishment actions based on potentially manipulable inputs requires strict validation of punishment configurations.

- **Data Exposure:**  
  Unrestricted access to violation histories can lead to privacy concerns and potential abuse.

- **Rate Limiting:**  
  Absence of controls on how frequently violations can be added may allow abuse or performance degradation.

- **Memory Usage:**  
  Without persistence or cleanup strategies, violation histories can accumulate and impact server memory over time.

## Recommendations for Improvement

- Use thread-safe collections or synchronize access to violation history lists to prevent concurrency issues.

- Sanitize and validate all input strings before storage or display to mitigate injection risks.

- Secure punishment configuration management to prevent unauthorized or malicious modifications.

- Implement permission checks to restrict access to violation histories.

- Introduce rate limiting or throttling mechanisms on violation additions.

- Consider persisting violation data externally and cleaning up histories for inactive players to manage memory usage effectively.