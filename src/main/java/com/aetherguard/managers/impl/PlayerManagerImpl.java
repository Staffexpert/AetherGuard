package com.aetherguard.managers.impl;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import com.aetherguard.managers.interfaces.IPlayerManager;
import org.bukkit.entity.Player;

public class PlayerManagerImpl extends PlayerManager implements IPlayerManager {
    public PlayerManagerImpl(AetherGuard plugin) {
        super(plugin);
    }
}