package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * BaseCommand - Lightweight, secure command base class
 * Version: v1.2.0
 *
 * <p>Provides helpers for permission checks, argument parsing and safe
 * player-only handling. Implementations should remain side-effect free
 * where possible and defer heavy work to async tasks.</p>
 */
public abstract class BaseCommand extends AetherGuardCommand {

    private final boolean playerOnly;

    protected BaseCommand(AetherGuard plugin, String name, String permission, boolean playerOnly) {
        super(plugin, name, "", "", permission);
        this.playerOnly = playerOnly;
    }

    @Override
    public boolean isPlayerOnly() { return playerOnly; }

    protected final boolean checkPermission(CommandSender sender) {
        return getPermission() == null || sender.hasPermission(getPermission());
    }

    protected final Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player) sender;
        sender.sendMessage("This command can only be used by a player.");
        return null;
    }

    protected final void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage("Usage: " + usage);
    }

    @Override
    public String[] onTabComplete(CommandSender sender, String[] args) {
        List<String> result = onTabCompleteList(sender, args);
        return result != null ? result.toArray(new String[0]) : new String[0];
    }

    public List<String> onTabCompleteList(CommandSender sender, String[] args) { return Collections.emptyList(); }
}