package com.github.gavvydizzle.skillsplugin.skill;

import com.github.mittenmc.serverutils.Numbers;

/**
 * Represents an ability which has a minimum level and an equation to determine activation
 */
public class PassiveAbility {

    private final int minimumLevel;
    private final String equation;

    public PassiveAbility(int minimumLevel, String equation) {
        this.minimumLevel = minimumLevel;
        this.equation = equation == null || equation.trim().isEmpty() ? "0" : equation;
    }

    /**
     * Determines if this ability should activate given the level and the value of the equation
     * @param level The level of this skill
     * @return True if this skill should activate
     */
    public boolean shouldActivate(int level) {
        if (level < minimumLevel) return false;
        return Numbers.percentChance(Numbers.eval(equation.replace("x", "" + level)));
    }
}
