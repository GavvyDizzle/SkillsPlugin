package com.github.gavvydizzle.skillsplugin.skill;

import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class SkillManager {

    private final ArrayList<Skill> skills;

    public SkillManager() {
        skills = new ArrayList<>();
        reload();
    }

    public void registerSkill(Skill skill) {
        skills.add(skill);
    }

    public void reload() {
        AbilitiesConfig.get().options().copyDefaults(true);
        for (Skill skill : skills) {
            skill.reload();
        }
        AbilitiesConfig.save();
    }

    /**
     * @param skillType The SkillType
     * @return The skill associated with this type
     */
    @Nullable
    public Skill getSkillByType(SkillType skillType) {
        for (Skill skill : skills) {
            if (skill.getSkillType() == skillType) return skill;
        }
        return null;
    }

}
