package com.aetherguard.managers;

import com.aetherguard.commands.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 🛡️ AetherGuard Command Manager
 * 
 * Manages all anti-cheat commands and tab completion
 * Handles command routing and permissions
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class CommandManager implements CommandExecutor, TabCompleter {
    
    private final AetherGuard plugin;
    private final Map<String, AetherGuardCommand> commands;
    private final Map<String, String> aliases;
    
    public CommandManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.aliases = new HashMap<>();
    }
    
    /**
     * Register all commands
     */
    public void registerCommands() {
        // Register main command
        plugin.getCommand("aetherguard").setExecutor(this);
        plugin.getCommand("aetherguard").setTabCompleter(this);
        
        // Register alias
        plugin.getCommand("ag").setExecutor(this);
        plugin.getCommand("ag").setTabCompleter(this);
        
        // Initialize commands
        initializeCommands();
        
        plugin.getLogger().info("§7Registered " + commands.size() + " commands");
    }
    
    /**
     * Initialize all commands
     */
    private void initializeCommands() {
        // Commands will be registered when implemented
    }
    
    /**
     * Register a command
     */
    private void registerCommand(AetherGuardCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        
        // Register aliases
        for (String alias : command.getAliases()) {
            aliases.put(alias.toLowerCase(), command.getName().toLowerCase());
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Handle no arguments
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        // Get command name
        String commandName = args[0].toLowerCase();
        
        // Check aliases
        String actualCommandName = aliases.getOrDefault(commandName, commandName);
        
        // Get command
        AetherGuardCommand command = commands.get(actualCommandName);
        if (command == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.unknown-command", 
                "command", commandName));
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission(command.getPermission())) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }
        
        // Check if player-only command
        if (command.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }
        
        // Execute command
        try {
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            command.execute(sender, commandArgs);
            
            return true;
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.command-error", 
                "error", e.getMessage()));
            plugin.getLogger().warning("§cError executing command " + commandName + ": " + e.getMessage());
            return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Tab complete main command
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            
            for (AetherGuardCommand command : commands.values()) {
                if (sender.hasPermission(command.getPermission()) && 
                    command.getName().toLowerCase().startsWith(partial)) {
                    completions.add(command.getName());
                }
            }
            
            return completions;
        }
        
        // Tab complete sub-command
        if (args.length > 1) {
            String commandName = args[0].toLowerCase();
            String actualCommandName = aliases.getOrDefault(commandName, commandName);
            
            AetherGuardCommand command = commands.get(actualCommandName);
            if (command != null && sender.hasPermission(command.getPermission())) {
                String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
                String[] tabComplete = command.onTabComplete(sender, commandArgs);
                return Arrays.asList(tabComplete);
            }
        }
        
        return completions;
    }
    
    /**
     * Show help to sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help-header", 
            "page", "1"));
        
        List<AetherGuardCommand> availableCommands = new ArrayList<>();
        
        for (AetherGuardCommand command : commands.values()) {
            if (sender.hasPermission(command.getPermission())) {
                availableCommands.add(command);
            }
        }
        
        // Sort commands by name
        availableCommands.sort(Comparator.comparing(AetherGuardCommand::getName));
        
        int page = 1;
        int commandsPerPage = 10;
        int startIndex = (page - 1) * commandsPerPage;
        int endIndex = Math.min(startIndex + commandsPerPage, availableCommands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            AetherGuardCommand command = availableCommands.get(i);
            sender.sendMessage(plugin.getConfigManager().getMessage("commands.help-entry",
                "command", command.getName(),
                "description", command.getDescription(),
                "usage", command.getUsage()
            ));
        }
        
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help-footer"));
    }
    
    /**
     * Get all commands
     */
    public Collection<AetherGuardCommand> getCommands() {
        return new ArrayList<>(commands.values());
    }
    
    /**
     * Get command by name
     */
    public AetherGuardCommand getCommand(String name) {
        String actualName = aliases.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return commands.get(actualName);
    }
}