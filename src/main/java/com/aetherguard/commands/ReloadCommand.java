package com.aetherguard.commands.subcommands;

import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(AetherGuard plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Recarga la configuración del plugin.";
    }

    @Override
    public String getSyntax() {
        return "/ag reload";
    }

    @Override
    public String getPermission() {
        return "aetherguard.admin.reload";
    }

    @Override
    public boolean isConsoleAllowed() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().loadConfigurations();
        sender.sendMessage(ChatColor.GREEN + "AetherGuard ha sido recargado correctamente.");
        plugin.getAetherGuardLogger().info("Configuración recargada por " + sender.getName() + ".");
    }
}