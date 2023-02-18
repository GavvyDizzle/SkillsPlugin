package com.github.gavvydizzle.skillsplugin.skill;

import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;

public class ActiveAbility {

    private final int minimumLevel, cooldownSeconds;

    public ActiveAbility(int minimumLevel, int cooldownSeconds) {
        this.minimumLevel = minimumLevel;
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Determines if the ability can activate given the level and last use time
     * @param level The level
     * @param timeSinceLastUse The time in milliseconds since the last use
     * @return True if this ability can activate
     */
    public boolean canAbilityActivate(int level, long timeSinceLastUse) {
        if (level < minimumLevel) return false;
        return !isCooldownActive(timeSinceLastUse);
    }

    /**
     * Determines if this ability can be used given the last time it was used.
     * @param timeSinceLastUse The time in milliseconds since the last use
     * @return True if the cooldown is active
     */
    public boolean isCooldownActive(long timeSinceLastUse) {
        return timeSinceLastUse < cooldownSeconds * 1000L;
    }

    /**
     * @param timeSinceLastUse The time in milliseconds since the last use
     * @return The time formatted to string form, "&eNone" if the cooldown is not active
     */
    public String getTimeUntilNextUse(long timeSinceLastUse) {
        return Numbers.getTimeFormatted((int) (cooldownSeconds / (timeSinceLastUse/1000)), Colors.conv("&eNone"));
    }

}
