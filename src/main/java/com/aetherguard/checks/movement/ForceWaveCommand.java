package com.aetherguard.commands.subcommands;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.BanWaveManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ForceWaveCommand extends SubCommand {

    public ForceWaveCommand(AetherGuard plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "forcewave";
    }

    @Override
    public String getDescription() {
        return "Inicia manualmente una ola de baneos.";
    }

    @Override
    public String getSyntax() {
        return "/ag forcewave";
    }

    @Override
    public String getPermission() {
        return "aetherguard.admin.forcewave";
    }

    @Override
    public boolean isConsoleAllowed() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getBanWaveManager().forceWave();
        sender.sendMessage(ChatColor.GREEN + "Forzando una ola de baneos...");
        plugin.getAetherGuardLogger().info("Ban wave forzada por " + sender.getName());
    }
}