package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * 🛡️ AetherGuard Reload Command
 *
 * Reloads all configuration files for the plugin.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(AetherGuard plugin) {
        super(plugin, "reload", "aetherguard.reload", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Reloading AetherGuard configurations...");
        plugin.getConfigManager().reloadConfigurations();
        plugin.getCheckManager().reloadChecks();
        sender.sendMessage(ChatColor.GREEN + "AetherGuard has been reloaded successfully!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}