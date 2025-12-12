package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.gui.MainGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 🛡️ AetherGuard GUI Command
 *
 * Opens the main control panel GUI.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class GUICommand extends BaseCommand {

    public GUICommand(AetherGuard plugin) {
        super(plugin, "gui", "aetherguard.gui", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            new MainGUI(plugin, player).open();
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /ag gui");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}