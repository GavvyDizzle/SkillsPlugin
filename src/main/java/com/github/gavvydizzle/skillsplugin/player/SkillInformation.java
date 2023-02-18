package com.github.gavvydizzle.skillsplugin.player;

import com.github.gavvydizzle.skillsplugin.skill.SkillType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Stores all information about all of a player's skills
 */
public class SkillInformation {

    private final SkillWrapper archery, excavation, farming, fishing, foraging, hunter, mining, slayer;
    private final ArrayList<SkillWrapper> list;

    public SkillInformation() {
        archery = new SkillWrapper();
        excavation = new SkillWrapper();
        farming = new SkillWrapper();
        fishing = new SkillWrapper();
        foraging = new SkillWrapper();
        hunter = new SkillWrapper();
        mining = new SkillWrapper();
        slayer = new SkillWrapper();

        list = new ArrayList<>(8);
        list.addAll(Arrays.asList(archery, excavation, farming, fishing, foraging, hunter, mining, slayer));
    }

    public int getLevel(SkillType skillType) {
        switch (skillType) {
            case ARCHERY:
                return archery.getLevel();
            case EXCAVATION:
                return excavation.getLevel();
            case FARMING:
                return farming.getLevel();
            case FISHING:
                return fishing.getLevel();
            case FORAGING:
                return foraging.getLevel();
            case HUNTER:
                return hunter.getLevel();
            case MINING:
                return mining.getLevel();
            case SLAYER:
                return slayer.getLevel();
            default:
                return 0;
        }
    }

    public void setLevel(SkillType skillType, int level) {
        switch (skillType) {
            case ARCHERY:
                archery.setLevel(level);
                break;
            case EXCAVATION:
                excavation.setLevel(level);
                break;
            case FARMING:
                farming.setLevel(level);
                break;
            case FISHING:
                fishing.setLevel(level);
                break;
            case FORAGING:
                foraging.setLevel(level);
                break;
            case HUNTER:
                hunter.setLevel(level);
                break;
            case MINING:
                mining.setLevel(level);
                break;
            case SLAYER:
                slayer.setLevel(level);
                break;
        }
    }

    public long getTotalXP(SkillType skillType) {
        switch (skillType) {
            case ARCHERY:
                return archery.getXp();
            case EXCAVATION:
                return excavation.getXp();
            case FARMING:
                return farming.getXp();
            case FISHING:
                return fishing.getXp();
            case FORAGING:
                return foraging.getXp();
            case HUNTER:
                return hunter.getXp();
            case MINING:
                return mining.getXp();
            case SLAYER:
                return slayer.getXp();
            default:
                return 0;
        }
    }

    public void setTotalXP(SkillType skillType, long xp) {
        switch (skillType) {
            case ARCHERY:
                archery.setXp(xp);
                break;
            case EXCAVATION:
                excavation.setXp(xp);
                break;
            case FARMING:
                farming.setXp(xp);
                break;
            case FISHING:
                fishing.setXp(xp);
                break;
            case FORAGING:
                foraging.setXp(xp);
                break;
            case HUNTER:
                hunter.setXp(xp);
                break;
            case MINING:
                mining.setXp(xp);
                break;
            case SLAYER:
                slayer.setXp(xp);
                break;
        }
    }

    public void addXP(SkillType skillType, long xp) {
        switch (skillType) {
            case ARCHERY:
                archery.addXP(xp);
                break;
            case EXCAVATION:
                excavation.addXP(xp);
                break;
            case FARMING:
                farming.addXP(xp);
                break;
            case FISHING:
                fishing.addXP(xp);
                break;
            case FORAGING:
                foraging.addXP(xp);
                break;
            case HUNTER:
                hunter.addXP(xp);
                break;
            case MINING:
                mining.addXP(xp);
                break;
            case SLAYER:
                slayer.addXP(xp);
                break;
        }
    }

    /**
     * Get the time this skill's ability was last used
     * @param skillType The SkillType
     * @return The time in milliseconds
     */
    public long getLastUseTime(SkillType skillType) {
        switch (skillType) {
            case ARCHERY:
                return archery.getLastTimeUsed();
            case EXCAVATION:
                return excavation.getLastTimeUsed();
            case FARMING:
                return farming.getLastTimeUsed();
            case FISHING:
                return fishing.getLastTimeUsed();
            case FORAGING:
                return foraging.getLastTimeUsed();
            case HUNTER:
                return hunter.getLastTimeUsed();
            case MINING:
                return mining.getLastTimeUsed();
            case SLAYER:
                return slayer.getLastTimeUsed();
            default:
                return 0;
        }
    }

    /**
     * Get the time since this skill's ability was last used
     * @param skillType The SkillType
     * @return The difference in milliseconds
     */
    public long getTimeSinceLastUse(SkillType skillType) {
        long time = System.currentTimeMillis();

        switch (skillType) {
            case ARCHERY:
                return time - archery.getLastTimeUsed();
            case EXCAVATION:
                return time - excavation.getLastTimeUsed();
            case FARMING:
                return time - farming.getLastTimeUsed();
            case FISHING:
                return time - fishing.getLastTimeUsed();
            case FORAGING:
                return time - foraging.getLastTimeUsed();
            case HUNTER:
                return time - hunter.getLastTimeUsed();
            case MINING:
                return time - mining.getLastTimeUsed();
            case SLAYER:
                return time - slayer.getLastTimeUsed();
            default:
                return -1;
        }
    }

    /**
     * Set this skill's last use time
     * @param skillType The SkillType to set
     * @param time The time to set it to
     */
    public void setLastUseTime(SkillType skillType, long time) {
        switch (skillType) {
            case ARCHERY:
                archery.setLastTimeUsed(time);
                break;
            case EXCAVATION:
                excavation.setLastTimeUsed(time);
                break;
            case FARMING:
                farming.setLastTimeUsed(time);
                break;
            case FISHING:
                fishing.setLastTimeUsed(time);
                break;
            case FORAGING:
                foraging.setLastTimeUsed(time);
                break;
            case HUNTER:
                hunter.setLastTimeUsed(time);
                break;
            case MINING:
                mining.setLastTimeUsed(time);
                break;
            case SLAYER:
                slayer.setLastTimeUsed(time);
                break;
        }
    }

    /**
     * Set this skill's last use time to now
     * @param skillType The SkillType to set
     */
    public void setLastUseTime(SkillType skillType) {
        switch (skillType) {
            case ARCHERY:
                archery.setLastTimeUsedNow();
                break;
            case EXCAVATION:
                excavation.setLastTimeUsedNow();
                break;
            case FARMING:
                farming.setLastTimeUsedNow();
                break;
            case FISHING:
                fishing.setLastTimeUsedNow();
                break;
            case FORAGING:
                foraging.setLastTimeUsedNow();
                break;
            case HUNTER:
                hunter.setLastTimeUsedNow();
                break;
            case MINING:
                mining.setLastTimeUsedNow();
                break;
            case SLAYER:
                slayer.setLastTimeUsedNow();
                break;
        }
    }

    /**
     * Sets the level of all skills based on the xp amount
     */
    public void updateLevels() {
        for (SkillWrapper skillWrapper : list) {
            skillWrapper.updateLevel();
        }
    }

    @Override
    public String toString() {
        return "archery: " + archery.toString() + "\n" +
                "excavation: " + excavation.toString() + "\n" +
                "farming: " + farming.toString() + "\n" +
                "fishing: " + fishing.toString() + "\n" +
                "foraging: " + foraging.toString() + "\n" +
                "hunter: " + hunter.toString() + "\n" +
                "mining: " + mining.toString() + "\n" +
                "slayer: " + slayer.toString();
    }

}
