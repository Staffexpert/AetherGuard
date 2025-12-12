package com.aetherguard.checks.movement;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard Speed Type A Check
 * 
 * Basic horizontal speed detection
 * Detects excessive horizontal movement speed
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class SpeedA extends MovementCheck {
    
    public SpeedA(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        return CheckType.MOVEMENT_SPEED;
    }
    
    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }
        
        MoveData move = getMoveData(data);
        
        // Check for speed violations
        if (isSpeedViolation(player, move)) {
            return CheckResult.violation("Speed hack detected", 
                String.format("Speed: %.3f, Expected: %.3f", move.horizontalDistance, calculateExpectedSpeed(player, move.from, move.to)));
        }
        
        return CheckResult.pass();
    }
    
    private boolean isSpeedViolation(Player player, MoveData move) {
        if (!move.isMovingHorizontally()) {
            return false;
        }
        
        double expectedSpeed = calculateExpectedSpeed(player, move.from, move.to);
        double tolerance = getTolerance(player);
        
        return move.horizontalDistance > (expectedSpeed + tolerance);
    }
}