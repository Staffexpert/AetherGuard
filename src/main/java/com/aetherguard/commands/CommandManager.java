package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Modern Command Manager - Sistema de comandos unificado
 */
public class CommandManager implements CommandExecutor, TabCompleter {
    private final AetherGuard plugin;
    private final Map<String, BaseCommand> subCommands;

    public CommandManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        registerSubCommands();
    }

    private void registerSubCommands() {
        // Register existing commands as subcommands
        subCommands.put("banwave", new BanWaveCommand(plugin));
        // TODO: Add more subcommands as needed
    }

    public void registerCommands() {
        plugin.getCommand("aetherguard").setExecutor(this);
        plugin.getCommand("aetherguard").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        BaseCommand cmd = subCommands.get(subCommand);

        if (cmd == null) {
            sender.sendMessage("§cUnknown subcommand. Use /aetherguard help");
            return true;
        }

        if (!cmd.checkPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        cmd.execute(sender, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            for (String subCmd : subCommands.keySet()) {
                if (subCmd.startsWith(partial)) {
                    completions.add(subCmd);
                }
            }
            return completions;
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            BaseCommand cmd = subCommands.get(subCommand);
            if (cmd != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return cmd.onTabCompleteList(sender, subArgs);
            }
        }
        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== AetherGuard Commands ===");
        for (Map.Entry<String, BaseCommand> entry : subCommands.entrySet()) {
            if (entry.getValue().checkPermission(sender)) {
                sender.sendMessage("§e/aetherguard " + entry.getKey() + " §7- " + entry.getValue().getDescription());
            }
        }
    }

    public void registerSubCommand(String name, BaseCommand subCommand) {
        subCommands.put(name.toLowerCase(), subCommand);
    }
}