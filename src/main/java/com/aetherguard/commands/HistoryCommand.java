package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 🛡️ AetherGuard History Command
 *
 * Displays the punishment history for a specific player.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(AetherGuard plugin) {
        super(plugin, "history", "aetherguard.history", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ag history <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        // Placeholder for fetching history from DatabaseManager
        sender.sendMessage(ChatColor.GOLD + "Fetching punishment history for " + target.getName() + "...");
        sender.sendMessage(ChatColor.GRAY + "(Database integration required for this feature)");
        // Example: List<Punishment> history = plugin.getDatabaseManager().getPlayerHistory(target.getUniqueId());
        // for (Punishment p : history) {
        //     sender.sendMessage(ChatColor.YELLOW + "- " + p.getType() + " for " + p.getReason() + " on " + p.getDate());
        // }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return null;
    }
}