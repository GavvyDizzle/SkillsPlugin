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
 * EXCAVATION SKILL
 * Obtaining XP: Mining shovel blocks, making paths
 * Passive Abilities:
 * - Chance to make mined blocks drop double
 * Active Ability: TODO
 */
public class Excavation extends Skill {

    private PassiveAbility arrowHarming, criticalHit;
    private ActiveAbility activeAbility;
    private int harmingLevel, arrowSpreadTicks;
    private String criticalHitDamageEquation, numArrowsEquation;
    private final HashMap<Material, Integer> materialMap;

    public Excavation(LevelManager levelManager) {
        super(SkillType.EXCAVATION, levelManager);
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
