# Security Vulnerability Report for `ActionManager` Class

This report identifies potential security vulnerabilities in the provided `ActionManager` Java class code, which is part of a Minecraft plugin managing punishment actions.

---

## 1. Command Injection via `handleCommand`

### Description
The `handleCommand` method constructs and executes a command string that can include user-controllable data:

```java
String command = action.getParameter()
        .replace("%player%", player.getName())
        .replace("%uuid%", player.getUniqueId().toString())
        .replace("%reason%", result.getReason())
        .replace("%check%", check.getFullName());

if (!command.startsWith("/")) {
    command = "/" + command;
}

Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
```

- The command string is directly dispatched from the console sender.
- The `action.getParameter()` is used as the base command string and may come from configuration or external input.
- The replacements include `%reason%` and `%check%`, which may contain user-generated or attacker-controlled content.

### Risk
- If an attacker can influence the `action.getParameter()` or the placeholders (`%reason%`, `%check%`), they may inject arbitrary commands executed with console privileges.
- This can lead to privilege escalation, server compromise, or data manipulation.

### Recommendations
- Strictly validate and sanitize the command string and all placeholders before execution.
- Implement a whitelist of allowed commands or parameters.
- Avoid executing commands constructed from untrusted input.
- Consider using safer APIs or command execution methods that do not allow arbitrary command injection.

---

## 2. Insufficient Validation in `handleTempBan`

### Description
The `handleTempBan` method parses a duration and reason from the action parameter:

```java
String[] parts = parameter.split(" ", 2);

if (parts.length < 2) {
    plugin.getLogger().warning("§cInvalid tempban format: " + parameter);
    return;
}

String durationStr = parts[0];
String reason = parts[1];

long duration = parseDuration(durationStr);
if (duration <= 0) {
    plugin.getLogger().warning("§cInvalid duration format: " + durationStr);
    return;
}
```

- The duration is parsed from a string that may be attacker-controlled.
- The reason is used directly in ban messages and kick messages.
- The ban expiry is scheduled using the parsed duration.

### Risk
- Malformed or malicious input could cause unexpected behavior.
- The reason string is not sanitized before being used in ban messages or logs, potentially leading to log injection or message spoofing.
- The duration parsing does not limit maximum duration, which could be abused to create excessively long bans or cause scheduler overload.

### Recommendations
- Sanitize and validate the reason string to prevent injection attacks.
- Enforce maximum and minimum duration limits to prevent abuse.
- Handle parsing errors gracefully and log suspicious inputs.
- Consider escaping or filtering special characters in reason strings.

---

## 3. Lack of Input Validation in `parseDuration`

### Description
The `parseDuration` method parses duration strings with suffixes:

```java
private long parseDuration(String durationStr) {
    try {
        if (durationStr.endsWith("s")) {
            return Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
        } else if (durationStr.endsWith("m")) {
            return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 60;
        } else if (durationStr.endsWith("h")) {
            return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 3600;
        } else if (durationStr.endsWith("d")) {
            return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 86400;
        } else {
            return Long.parseLong(durationStr) * 60; // Default to minutes
        }
    } catch (NumberFormatException e) {
        return -1;
    }
}
```

### Risk
- No upper bound checks on parsed duration values.
- Negative or zero durations are handled by returning -1, but callers must check this.
- Extremely large values could cause integer overflow or scheduler abuse.

### Recommendations
- Add validation to enforce reasonable duration limits (e.g., max ban length).
- Reject negative or zero durations explicitly.
- Log suspicious or out-of-range duration inputs.

---

## 4. Potential Race Condition in `executeAction` Cooldown Handling

### Description
Cooldowns are managed using a `ConcurrentHashMap`:

```java
UUID uuid = player.getUniqueId();
long currentTime = System.currentTimeMillis();
Long lastTime = lastActionTimes.get(uuid);

if (lastTime != null && (currentTime - lastTime) < actionInfo.getCooldown()) {
    return; // Cooldown not passed
}
```

- The check and update of `lastActionTimes` are not atomic.
- When delay > 0, the update happens inside a delayed task, potentially allowing multiple actions to bypass cooldown if called rapidly.

### Risk
- Attackers could exploit this to bypass cooldown restrictions by triggering multiple actions quickly.
- Could lead to denial of service or excessive punishment actions.

### Recommendations
- Use atomic operations or synchronization to ensure cooldown checks and updates are consistent.
- Consider using `putIfAbsent` or `compute` methods of `ConcurrentHashMap` for atomic updates.
- Ensure delayed tasks also respect cooldown logic.

---

## 5. Missing Implementation and Validation in `handleFreeze` and `handleWebhook`

### Description
- `handleFreeze` is marked as TODO and currently only logs the freeze action.
- `handleWebhook` is also a placeholder.

### Risk
- Incomplete implementations may lead to inconsistent state or security gaps.
- If these methods are later implemented without proper validation, they could introduce vulnerabilities.

### Recommendations
- Complete implementations with proper input validation and security checks.
- Ensure webhook URLs and payloads are validated and sanitized.
- Implement freeze logic securely to prevent abuse.

---

## 6. Logging Sensitive Information

### Description
The `handleLog` method logs detailed information including player UUID, check names, reasons, and confidence levels.

### Risk
- Logs may contain sensitive or personally identifiable information (PII).
- If logs are accessible to unauthorized users, this could lead to privacy violations.

### Recommendations
- Ensure logs are stored securely with proper access controls.
- Consider anonymizing or redacting sensitive data if logs are exposed.
- Follow data protection best practices.

---

# Summary

| Vulnerability                      | Severity | Recommendation Summary                          |
|----------------------------------|----------|------------------------------------------------|
| Command Injection in `handleCommand` | High     | Sanitize inputs, whitelist commands, avoid direct execution of untrusted commands |
| Insufficient Validation in `handleTempBan` | Medium   | Sanitize reason, validate duration, limit ban length |
| Lack of Input Validation in `parseDuration` | Medium   | Enforce duration bounds, reject invalid inputs |
| Race Condition in Cooldown Handling | Medium   | Use atomic operations for cooldown checks and updates |
| Incomplete Implementations (`handleFreeze`, `handleWebhook`) | Low      | Complete with secure validation and logic |
| Logging Sensitive Information     | Low      | Secure logs, consider data anonymization |

---

# Final Notes

- The plugin handles punishment actions with potentially high privileges; security is critical.
- Input validation and sanitization must be enforced consistently.
- Command execution is the highest risk area and should be carefully controlled.
- Regular security reviews and testing are recommended to maintain plugin security.