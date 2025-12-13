package com.aetherguard.core.systems;

import com.aetherguard.checks.Check;
import java.util.Collection;

public interface ICheckSystem {
    void registerCheck(Check check);

    void unregisterCheck(String checkName);

    Check getCheck(String checkName);

    Collection<Check> getAllChecks();

    Collection<Check> getChecksByCategory(String category);

    void enableCheck(String checkName);

    void disableCheck(String checkName);

    int getTotalChecks();

    int getEnabledChecks();
}
