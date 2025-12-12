package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    
    private final AetherGuard plugin;
    private final Map<String, AetherGuardCommand> commands = new HashMap<>();
    
    public CommandManager(AetherGuard plugin) {
        this.plugin = plugin;
    }
    
    public void registerCommands() {
    }
    
    public void executeCommand(String label, CommandSender sender, String[] args) {
        AetherGuardCommand cmd = commands.get(label.toLowerCase());
        if (cmd == null) {
            sender.sendMessage("§cUnknown command");
            return;
        }
        
        if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
            sender.sendMessage("§cYou don't have permission to use this command");
            return;
        }
        
        cmd.execute(sender, args);
    }
    
    public void registerCommand(AetherGuardCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }
    
    public void registerReloadCommand() {}
    public void registerToggleCommand() {}
    public void registerVerboseCommand() {}
    public void registerAlertsCommand() {}
    public void registerDebugCommand() {}
    public void registerProfileCommand() {}
    public void registerExemptCommand() {}
    public void registerPunishCommand() {}
    public void registerStatsCommand() {}
    public void registerPingCommand() {}
    public void registerVLCommand() {}
    public void registerTPSCommand() {}
    public void registerFreezeCommand() {}
}
