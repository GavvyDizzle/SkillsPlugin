package com.github.gavvydizzle.skillsplugin.player;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;

/**
 * Stores the level, total xp, and last ability use time for a player's skill
 */
public class SkillWrapper {

    private int level;
    private long xp, lastTimeUsed;

    public SkillWrapper() {
        this.level = 0;
        this.xp = 0;
        this.lastTimeUsed = 0;
    }

    @Override
    public String toString() {
        return "level=" + level + " xp=" + xp + " lastTimeUsed=" + lastTimeUsed;
    }

    public int getLevel() {
        return level;
    }

    public void updateLevel() {
        this.level = SkillsPlugin.getInstance().getLevelManager().getLevel(xp);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public void addXP(long xp) {
        this.xp += xp;
    }

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed(long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }

    public void setLastTimeUsedNow() {
        this.lastTimeUsed = System.currentTimeMillis();
    }
}
