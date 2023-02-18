package com.github.gavvydizzle.skillsplugin.skill.skills;

import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.ActiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.PassiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.Skill;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * FISHING SKILL
 * Obtaining XP: Killing entities with arrow
 * Passive Abilities:
 * - Increase hook rate
 * - Add items into drop pool?
 * Active Ability: Every reel in when the hook is in water will give a catch
 */
public class Fishing extends Skill {

    private PassiveAbility arrowHarming, criticalHit;
    private ActiveAbility activeAbility;
    private int harmingLevel, arrowSpreadTicks;
    private String criticalHitDamageEquation, numArrowsEquation;
    private final HashMap<Material, Integer> materialMap;

    public Fishing(LevelManager levelManager) {
        super(SkillType.FISHING, levelManager);
        materialMap = new HashMap<>();
        reload();
    }

    @Override
    public void reload() {
    }

    @Override
    public void startActiveAbility(Player player) {

    }
}
