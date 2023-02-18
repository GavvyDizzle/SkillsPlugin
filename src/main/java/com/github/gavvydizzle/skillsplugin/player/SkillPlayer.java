package com.github.gavvydizzle.skillsplugin.player;

import com.github.gavvydizzle.skillsplugin.gui.SkillInfoInventory;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * Represents a player who is loaded with this plugin.
 */
public class SkillPlayer {

    private final Player player;
    private final SkillInformation skillInformation;
    private final SkillInfoInventory skillInfoInventory;

    private final BossBar bossbar;
    private long lastSkillXPGainTime;

    /**
     * Creates a SkillPlayer with blank information.
     * This is intended to be used for players who have no save data.
     * @param player The player
     */
    public SkillPlayer(Player player) {
        this.player = player;
        this.skillInformation = new SkillInformation();
        this.skillInfoInventory = new SkillInfoInventory(this);

        bossbar = Bukkit.createBossBar(null, BarColor.BLUE, BarStyle.SOLID);
        lastSkillXPGainTime = 0;
    }

    /**
     * Creates a SkillPlayer with saved information
     * @param player The player
     * @param skillInformation The SkillInformation
     */
    public SkillPlayer(Player player, SkillInformation skillInformation) {
        this.player = player;
        this.skillInformation = skillInformation;
        this.skillInfoInventory = new SkillInfoInventory(this);

        bossbar = Bukkit.createBossBar(null, BarColor.BLUE, BarStyle.SOLID);
        lastSkillXPGainTime = 0;
    }

    /**
     * Saves the last time the player gained skill XP.
     * This value is used to determine when the bossbar should become invisible again.
     */
    public void onSkillXPGain() {
        lastSkillXPGainTime = System.currentTimeMillis();
        bossbar.setVisible(true);
    }

    public Player getPlayer() {
        return player;
    }

    public SkillInformation getSkillInformation() {
        return skillInformation;
    }

    public SkillInfoInventory getSkillInfoInventory() {
        return skillInfoInventory;
    }

    public BossBar getBossbar() {
        return bossbar;
    }

    public long getLastSkillXPGainTime() {
        return lastSkillXPGainTime;
    }

    @Override
    public String toString() {
        return player.getName() + " SkillPlayer\n" + skillInformation.toString();
    }
}
