package com.github.gavvydizzle.skillsplugin.skill;

import javax.annotation.Nullable;
import java.util.ArrayList;

public enum SkillType {
    ARCHERY, // Shooting entities with arrows
    EXCAVATION, // Shovel mining
    FARMING, // Breaking/planting crops
    FISHING, // Fishing items
    FORAGING, // Using axe to mine trees
    HUNTER, // Passive mobs (killing, breeding)
    MINING, // Mining with pickaxe, special things for ores
    SLAYER; // Hostile mobs

    public final String uppercaseName, lowercaseName;

    SkillType() {
        lowercaseName = name().toLowerCase();
        uppercaseName = lowercaseName.substring(0, 1).toUpperCase() + lowercaseName.substring(1);
    }

    public static final ArrayList<String> asStringList;

    static {
        asStringList = new ArrayList<>(SkillType.values().length);
        for (SkillType skillType : SkillType.values()) {
            asStringList.add(skillType.name().toLowerCase());
        }
    }

    @Nullable
    public static SkillType getFromString(String str) {
        for (SkillType skillType : SkillType.values()) {
            if (skillType.name().equalsIgnoreCase(str)) return skillType;
        }
        return null;
    }
}
