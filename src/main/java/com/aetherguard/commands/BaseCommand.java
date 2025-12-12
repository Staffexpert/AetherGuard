package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 🛡️ AetherGuard Base Command
 *
 * Abstract class representing a command for AetherGuard.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class BaseCommand {

    protected final AetherGuard plugin;
    private final String name;
    private final String permission;
    private final boolean playerOnly;

    public BaseCommand(AetherGuard plugin, String name, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    /**
     * Executes the command.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    public abstract void execute(CommandSender sender, String[] args);

    /**
     * Provides tab completions for the command.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     * @return A list of tab completions.
     */
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }
}