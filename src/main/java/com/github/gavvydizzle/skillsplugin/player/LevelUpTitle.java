package com.github.gavvydizzle.skillsplugin.player;

import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import org.bukkit.entity.Player;

public class LevelUpTitle {

    private final boolean isActive;
    private final String title, subtitle;

    public LevelUpTitle(boolean isActive, String title, String subtitle) {
        this.isActive = isActive;
        this.title = title.trim();
        this.subtitle = subtitle.trim();
    }

    /**
     * Displays the level up title for the player.
     * If the title is not active, nothing will happen.
     * If the title is blank, nothing will happen.
     * If the subtitle is blank, no subtitle will be shown.
     * @param player The player
     * @param skillType The SkillType
     * @param newLevel The level the player just reached
     * @return If the title was shown
     */
    public boolean showTitle(Player player, SkillType skillType, int newLevel) {
        if (!isActive || title.isEmpty()) return false;

        String newTitle = title.replace("{skill}", skillType.lowercaseName)
                .replace("{skill_capitalized}", skillType.uppercaseName)
                .replace("{level}", "" + newLevel);

        if (subtitle.isEmpty()) {
            player.sendTitle(title, null, 10, 70, 20);
        }
        else {
            String newSubtitle = subtitle.replace("{skill}", skillType.lowercaseName)
                    .replace("{skill_capitalized}", skillType.uppercaseName)
                    .replace("{level}", "" + newLevel);

            player.sendTitle(title, subtitle, 10, 70, 20);
        }
        return true;
    }

}
