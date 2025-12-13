package com.aetherguard.commands.subcommands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Clase base abstracta para todos los subcomandos de AetherGuard.
 */
public abstract class SubCommand {

    protected final AetherGuard plugin;

    public SubCommand(AetherGuard plugin) {
        this.plugin = plugin;
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract String getPermission();
    public abstract boolean isConsoleAllowed();
    public abstract void execute(CommandSender sender, String[] args);

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}