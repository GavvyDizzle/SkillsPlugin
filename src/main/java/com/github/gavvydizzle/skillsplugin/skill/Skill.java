package com.github.gavvydizzle.skillsplugin.skill;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Parent class for a skill.
 * Each skill is assumed to register listeners to handle abilities
 */
public abstract class Skill implements Listener {

    private final SkillType skillType;
    protected final LevelManager levelManager;

    private String bossbarTitle;
    private BarColor barColor;

    public Skill(SkillType skillType, LevelManager levelManager) {
        this.skillType = skillType;
        this.levelManager = levelManager;
    }

    /**
     * Handles what to do when the class is read in from file.
     * All subclasses should call this parent method in their reload method.
     * This must be done because child classes may need to initialize things before reading from the config.
     */
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(skillType.name() + ".bossbar.title", "&a" + skillType.uppercaseName + " Experience");
        config.addDefault(skillType.name() + ".bossbar.color", BarColor.GREEN.name());

        bossbarTitle = Colors.conv(config.getString(skillType.name() + ".bossbar.title"));

        String barColorString = config.getString(skillType.name() + ".bossbar.color");
        try {
            barColor = BarColor.valueOf(barColorString);
        } catch (Exception ignored) {
            SkillsPlugin.getInstance().getLogger().warning(barColorString + " is not a valid bossbar color! It is defaulting to GREEN");
            barColor = BarColor.GREEN;
        }

        //TODO - Do add method to update bar for skillPlayer
    }

    /**
     * Handles what to do when the active ability is started
     * @param player The player to start the ability for
     */
    public abstract void startActiveAbility(Player player);

    /**
     * Gets the player's level for this skill.
     * @param player The player
     * @return The level of this skill
     */
    public int getPlayerLevel(Player player) {
        return levelManager.getCachedLevel(skillType, player);
    }

    public void setActiveAbilityCooldown(Player player) {
        levelManager.onActiveAbilityUse(skillType, player);
    }

    public SkillType getSkillType() {
        return skillType;
    }

}
