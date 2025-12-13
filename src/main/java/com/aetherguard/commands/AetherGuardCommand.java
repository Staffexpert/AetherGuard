package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Objects;

/**
 * AetherGuardCommand - Compatibility wrapper for legacy commands
 * Version: v1.2.0
 *
 * <p>Lightweight container that can be adapted into the unified
 * `CommandManager` registration system.</p>
 */
public abstract class AetherGuardCommand {

    protected final AetherGuard plugin;
    protected final String name;
    protected final String description;
    protected final String usage;
    protected final String permission;

    protected AetherGuardCommand(AetherGuard plugin, String name, String description, String usage, String permission) {
        this.plugin = Objects.requireNonNull(plugin);
        this.name = Objects.requireNonNull(name);
        this.description = description == null ? "" : description;
        this.usage = usage == null ? "" : usage;
        this.permission = permission;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public String[] getAliases() { return new String[0]; }

    public boolean isPlayerOnly() { return false; }

    public String[] onTabComplete(CommandSender sender, String[] args) { return new String[0]; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUsage() { return usage; }
    public String getPermission() { return permission; }

    protected final String join(String[] args, int start) { return String.join(" ", Arrays.copyOfRange(args, start, args.length)); }
}
