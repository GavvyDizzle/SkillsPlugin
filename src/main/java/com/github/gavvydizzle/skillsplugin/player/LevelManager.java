package com.github.gavvydizzle.skillsplugin.player;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.LevelsConfig;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.gavvydizzle.skillsplugin.storage.Database;
import com.github.gavvydizzle.skillsplugin.utils.Pair;
import com.github.gavvydizzle.skillsplugin.utils.Sounds;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;
import java.util.*;

public class LevelManager implements Listener {

    private static final int MAX_LEVEL_XP_REMAINING = 1;
    private static final int MAX_LEVEL_XP_NEEDED = 0;
    private static final double MAX_LEVEL_PROGRESS = 1.0;
    private static final long BOSSBAR_VISIBILITY_MILLIS = 3000;

    private final Database database;

    private boolean unlimitedLevels;
    private int maxLevel;
    private int defaultLevelXP;
    private double globalMultiplier;
    private final HashMap<SkillType, Double> skillMultipliers; // Multipliers specific to each skill
    private final HashMap<Integer, Integer> levelRequirements; // Defines the relationship (level, xp) to get to this level
    // Contains the total XP needed to reach a level
    // The index is the same as the level (ex. XP for level 5 is stored at index 5)
    private long[] collectiveXP;
    private final HashMap<UUID, SkillPlayer> playerMap; // Cache of a player's levels

    private LevelUpTitle levelUpTitle;
    private boolean isLevelUpSoundEnabled, playerLevelUpSoundIfNoTitle;

    public LevelManager(Database database) {
        this.database = database;
        this.levelRequirements = new HashMap<>();
        this.skillMultipliers = new HashMap<>();
        this.playerMap = new HashMap<>();

        startBossbarClock();
        reload();
    }

    public void reload() {
        FileConfiguration config = LevelsConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("unlimitedLevels", true);
        config.addDefault("maxLevel", 50);
        config.addDefault("defaultXP_per_level", 10000);
        config.addDefault("globalMultiplier", 1.0);

        for (SkillType skillType : SkillType.values()) {
            config.addDefault("skillMultipliers." + skillType.name(), 1.0);
        }

        config.addDefault("levelUpTitle.enabled", true);
        config.addDefault("levelUpTitle.title", "&eLevel Up");
        config.addDefault("levelUpTitle.subTitle", "&a{skill_capitalized} is now level {level}");
        config.addDefault("levelUpTitle.sound.enabled", true);
        config.addDefault("levelUpTitle.sound.playIfNoTitle", false);

        setDefinedLevelDefaults(config);

        LevelsConfig.save();

        unlimitedLevels = config.getBoolean("unlimitedLevels");
        maxLevel = unlimitedLevels ? Integer.MAX_VALUE : config.getInt("maxLevel");
        defaultLevelXP = config.getInt("defaultXP_per_level");
        globalMultiplier = config.getDouble("globalMultiplier");

        levelUpTitle = new LevelUpTitle(
                config.getBoolean("levelUpTitle.enabled"),
                Colors.conv(config.getString("levelUpTitle.title")),
                Colors.conv(config.getString("levelUpTitle.subTitle"))
        );
        isLevelUpSoundEnabled = config.getBoolean("levelUpTitle.sound.enabled");
        playerLevelUpSoundIfNoTitle = config.getBoolean("levelUpTitle.sound.playIfNoTitle");

        skillMultipliers.clear();
        for (SkillType skillType : SkillType.values()) {
            double mult = config.getDouble("skillMultipliers." + skillType.name());
            if (mult < 0) {
                mult = 1.0;
                SkillsPlugin.getInstance().getLogger().warning("The multiplier for " + skillType.name() + " in levels.yml is invalid. It has been set to its default value");
            }

            skillMultipliers.put(skillType, mult);
        }

        levelRequirements.clear();
        int highestLevelDefined = 0;
        for (String key : Objects.requireNonNull(config.getConfigurationSection("definedLevels")).getKeys(false)) {
            String path = "definedLevels." + key;
            int level;

            try {
                level = Integer.parseInt(key);
            }
            catch (NumberFormatException e) {
                SkillsPlugin.getInstance().getLogger().warning(key + " at " + path + " is not a valid level. Ignoring it!");
                continue;
            }

            int xp = config.getInt(path);
            if (xp < 0) {
                xp = defaultLevelXP;
                SkillsPlugin.getInstance().getLogger().warning("The xp amount at "+ path + " in levels.yml is negative. It has been set to its default value");
            }
            levelRequirements.put(level, xp);

            if (level + 1 > highestLevelDefined) {
                highestLevelDefined = level + 1;
            }
        }

        int length = unlimitedLevels ? highestLevelDefined+1 : Math.min(maxLevel+1, highestLevelDefined+1);
        collectiveXP = new long[length];
        collectiveXP[0] = 0;
        for (int i = 1; i < collectiveXP.length; i++) {
            collectiveXP[i] = collectiveXP[i-1] + levelRequirements.getOrDefault(i, defaultLevelXP);
        }

        //TODO - Reload player levels on reload because levels/xp can change
    }

    private void setDefinedLevelDefaults (FileConfiguration config) {
        // https://hypixel-skyblock.fandom.com/wiki/Skills#Leveling (using levels 1-50)
        ArrayList<Integer> xp = new ArrayList<>(Arrays.asList(
                50,
                125,
                200,
                300,
                500,
                750,
                1000,
                1500,
                2000,
                3500,
                5000,
                7500,
                10000,
                15000,
                20000,
                30000,
                50000,
                75000,
                100000,
                200000,
                300000,
                400000,
                500000,
                600000,
                700000,
                800000,
                900000,
                1000000,
                1100000,
                1200000,
                1300000,
                1400000,
                1500000,
                1600000,
                1700000,
                1800000,
                1900000,
                2000000,
                2100000,
                2200000,
                2300000,
                2400000,
                2500000,
                2600000,
                2750000,
                2900000,
                3100000,
                3400000,
                3700000,
                4000000
        ));

        for (int i = 1; i <= xp.size(); i++) {
            config.addDefault("definedLevels." + i, xp.get(i-1));
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        SkillPlayer skillPlayer = new SkillPlayer(e.getPlayer(), database.loadPlayerInfo(e.getPlayer().getUniqueId()));
        playerMap.put(e.getPlayer().getUniqueId(), skillPlayer);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        database.updatePlayerInfo(playerMap.remove(e.getPlayer().getUniqueId()));
    }

    /**
     * Saves player data on server shutdown since PlayerQuitEvent is not called
     */
    public void savePlayersOnShutdown() {
        database.updatePlayerInfo(playerMap.values());
    }

    /**
     * Gets the level based on the amount of xp
     * @param xp The amount of xp. Must be positive
     * @return The highest whole level obtained
     */
    public int getLevel(long xp) {
        int level = getLevelBinarySearch(xp);
        if (level == collectiveXP.length-1) { // When level is the highest defined level
            if (!unlimitedLevels) {
                return maxLevel;
            }

            long remainingXP = xp - collectiveXP[collectiveXP.length-1];
            int numUndefinedLevelsCompleted = (int) (remainingXP / defaultLevelXP) + 1;

            return level + numUndefinedLevelsCompleted;
        }

        // Normal case where the level is in the defined bounds
        return level;
    }

    /**
     * Handles removing a player's bossbar after BOSSBAR_VISIBILITY_MILLIS has passed
     * since the last time the bossbar was updated
     */
    private void startBossbarClock() {
        new RepeatingTask(SkillsPlugin.getInstance(), 0, 1) {
            @Override
            public void run() {
                final long now = System.currentTimeMillis();

                for (SkillPlayer skillPlayer : playerMap.values()) {
                    if (now - skillPlayer.getLastSkillXPGainTime() > BOSSBAR_VISIBILITY_MILLIS) {
                        if (skillPlayer.getBossbar().isVisible()) skillPlayer.getBossbar().setVisible(false);
                    }
                }
            }
        };
    }

    /**
     * Gets the level of this skill cached for this player
     * @param skillType The skill type
     * @param player The player
     * @return The level of this skill for the player or 0 if an error occurs
     */
    public int getCachedLevel(SkillType skillType, Player player) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) {
            Bukkit.getLogger().severe(player.getName() + " failed to load as a SkillPlayer");
            return 0;
        }

        return skillPlayer.getSkillInformation().getLevel(skillType);
    }

    /**
     * Get the XP towards the next level
     * @param xp The amount of xp
     * @return The XP towards the next level
     */
    public long getXPTowardsNextLevel(long xp) {
        int level = getLevel(xp);
        if (!unlimitedLevels && level >= maxLevel) return MAX_LEVEL_XP_REMAINING;

        return xp - getTotalXPForLevel(level);
    }

    /**
     * Get the XP needed to reach the next level
     * @param xp The total amount of xp
     * @return The XP needed to reach the next level or
     */
    public long getXPUntilNextLevel(long xp) {
        Pair<Integer, Long> info = getLevelInformation(xp);
        if (!unlimitedLevels && info.first >= maxLevel) return MAX_LEVEL_XP_NEEDED;

        int xpNeeded = levelRequirements.getOrDefault(info.first+1, defaultLevelXP);
        return xpNeeded - info.second;
    }

    /**
     * Get how close this XP is to the next level. If at max level, this will return 1.0
     * @param xp The amount of xp
     * @return A double 0 <= x <= 1
     */
    public double getProgress(long xp) {
        Pair<Integer, Long> info = getLevelInformation(xp);
        if (!unlimitedLevels && info.first >= maxLevel) return MAX_LEVEL_PROGRESS;

        if (info.first + 1 >= collectiveXP.length) {
            return info.second * 1.0 / defaultLevelXP;
        }
        else {
            return info.second * 1.0 / (collectiveXP[info.first+1] - collectiveXP[info.first]);
        }
    }

    /**
     * Get the level and remaining XP
     * @param xp The amount of xp
     * @return A pair of values (level, XP towards next level)
     */
    public Pair<Integer, Long> getLevelInformation(long xp) {
        int level = getLevel(xp);
        if (!unlimitedLevels && level >= maxLevel) new Pair<>(level, MAX_LEVEL_XP_REMAINING);

        return new Pair<>(level, xp - getTotalXPForLevel(level));
    }

    /**
     * Adds experience to a player's skill.
     * This method takes multipliers into account.
     * @param skillType The SkillType
     * @param player The player
     * @param baseXP The amount of XP to add (before multipliers)
     */
    public void givePlayerExperience(SkillType skillType, Player player, long baseXP) {
        if (baseXP <= 0) return;

        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) return;

        skillPlayer.getSkillInformation().addXP(skillType, applyMultipliers(skillType, baseXP));
        attemptLevelUp(skillType, skillPlayer);

        //TODO - Bossbar
        //player.get
    }

    /**
     * Check if the player leveled up when given XP through skills.
     * If the player's level has changed, this will update the level cache and send level up visuals.
     * @param skillType The SkillType
     * @param skillPlayer The SkillPlayer
     */
    private void attemptLevelUp(SkillType skillType, SkillPlayer skillPlayer) {
        int oldLevel = skillPlayer.getSkillInformation().getLevel(skillType);
        int newLevel = getLevel(skillPlayer.getSkillInformation().getTotalXP(skillType));

        if (oldLevel != newLevel) {
            skillPlayer.getSkillInformation().setLevel(skillType, newLevel);

            boolean didTitleShow = levelUpTitle.showTitle(skillPlayer.getPlayer(), skillType, newLevel);
            if ((didTitleShow && isLevelUpSoundEnabled) || (isLevelUpSoundEnabled && playerLevelUpSoundIfNoTitle)) {
                Sounds.skillLevelUpSound.playSound(skillPlayer.getPlayer());
            }
        }
    }

    /**
     * Determines the amount of xp for this skill type after applying global and skill multipliers
     * @param skillType The skill type to use in the calculation
     * @param xp The amount of xp
     * @return The amount of xp (rounded down)
     */
    private long applyMultipliers(SkillType skillType, long xp) {
        return (long) (xp * skillMultipliers.get(skillType) * globalMultiplier);
    }

    /**
     * Sets the global multiplier
     * @param multiplier The multiplier to set it to
     * @return What the multiplier was set to or -1 if an error occurred
     */
    public double setGlobalMultiplier(double multiplier) {
        if (multiplier < 0) multiplier = 0.0;

        try {
            FileConfiguration config = SkillsPlugin.getInstance().getConfig();
            config.set("globalMultiplier", multiplier);
            SkillsPlugin.getInstance().saveConfig();

            globalMultiplier = multiplier;
            return multiplier;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Sets the multiplier for a specific skill
     * @param skillType The SkillType
     * @param multiplier The multiplier to set it to
     * @return What the multiplier was set to or -1 if an error occurred
     */
    public double setSkillMultiplier(SkillType skillType, double multiplier) {
        if (multiplier < 0) multiplier = 0.0;

        try {
            FileConfiguration config = SkillsPlugin.getInstance().getConfig();
            config.set("skillMultipliers." + skillType.name(), multiplier);
            SkillsPlugin.getInstance().saveConfig();

            skillMultipliers.put(skillType, multiplier);
            return multiplier;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Sets the multiplier for all skills to the same value
     * @param multiplier The multiplier to set them to
     * @return If all multipliers were updated successfully
     */
    public boolean setAllSkillMultipliers(double multiplier) {
        if (multiplier < 0) multiplier = 0.0;

        try {
            FileConfiguration config = SkillsPlugin.getInstance().getConfig();

            for (SkillType skillType : SkillType.values()) {
                config.set("skillMultipliers." + skillType.name(), multiplier);
                skillMultipliers.put(skillType, multiplier);
            }
            SkillsPlugin.getInstance().saveConfig();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void printMultipliers(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "-------(Skills Multipliers)-------");
        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Global Multiplier: " + Numbers.round(globalMultiplier, 3) + "x");
        for (SkillType skillType : skillMultipliers.keySet()) {
            double mult = skillMultipliers.get(skillType);
            sender.sendMessage(ChatColor.GREEN + skillType.name() + " Multiplier: " + Numbers.round(mult, 3) + "x" +
                    ChatColor.BLUE + " (" + Numbers.round(mult * globalMultiplier, 3) + "x)");
        }
        sender.sendMessage(ChatColor.GOLD + "-------(Skills Multipliers)-------");
        sender.sendMessage("");
    }

    /**
     * @param skillType The SkillType
     * @param player The player
     * @return The amount of milliseconds since the last time this active ability was used
     */
    public long getTimeSinceLastActiveAbilityUse(SkillType skillType, Player player) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) {
            Bukkit.getLogger().severe(player.getName() + " failed to load as a SkillPlayer");
            return -1;
        }

        return skillPlayer.getSkillInformation().getTimeSinceLastUse(skillType);
    }

    /**
     * Sets this player's active ability to used at this instant
     * @param skillType The SkillType
     * @param player The player
     */
    public void onActiveAbilityUse(SkillType skillType, Player player) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) {
            Bukkit.getLogger().severe(player.getName() + " failed to load as a SkillPlayer");
            return;
        }

        skillPlayer.getSkillInformation().setLastUseTime(skillType);
    }

    /**
     * @param level The level
     * @return The total amount of XP needed to reach this level exactly
     */
    private long getTotalXPForLevel(int level) {
        if (level <= 0) return 0;
        if (!unlimitedLevels && level > maxLevel) level = maxLevel;

        if (collectiveXP.length-1 >= level) {
            return collectiveXP[level];
        }
        else {
            int lastDefinedLevel = collectiveXP.length-1;
            int numUndefinedLevelsCompleted = level - lastDefinedLevel - 1;
            return collectiveXP[lastDefinedLevel] + (long) numUndefinedLevelsCompleted * defaultLevelXP;
        }
    }


    //*** Command Functions ***//

    /**
     * Set's the player's total XP as if they just reached this level
     * @param skillPlayer The SkillPlayer
     * @param level The level
     */
    private void updateXPToMatchLevel(SkillPlayer skillPlayer, SkillType skillType, int level) {
        if (level < 0) return;

        long newXP = getTotalXPForLevel(level);
        skillPlayer.getSkillInformation().setTotalXP(skillType, newXP);
    }

    /**
     * Sets a player's skill XP for a specific skill.
     * This method will NOT correctly set the totalXP if the level and xp are in disagreement
     * @param player The player
     * @param skillType The SkillType
     * @param percent The percent of the way to the next level [0,1)
     * @return If this method updated the value successfully
     */
    public boolean setPlayerSkillXP(Player player, SkillType skillType, double percent) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) return false;

        int currLevel = skillPlayer.getSkillInformation().getLevel(skillType);
        int xpForNextLevel = levelRequirements.getOrDefault(currLevel + 1, defaultLevelXP);
        long newXP = (long) (percent * xpForNextLevel);

        skillPlayer.getSkillInformation().setTotalXP(skillType, getTotalXPForLevel(currLevel) + newXP);
        return true;
    }

    /**
     * Sets a player's skill XP for a specific skill
     * @param player The player
     * @param skillType The SkillType
     * @param level The level to set the skill to (value will be lowered to the max level if applicable)
     * @return If this method updated the value successfully
     */
    public boolean setPlayerLevel(Player player, SkillType skillType, int level) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) return false;

        if (level > maxLevel) level = maxLevel;

        skillPlayer.getSkillInformation().setLevel(skillType, level);
        updateXPToMatchLevel(skillPlayer, skillType, level);
        return true;
    }



    @Nullable
    public Inventory getSkillInfoInventory(Player player) {
        SkillPlayer skillPlayer = playerMap.get(player.getUniqueId());
        if (skillPlayer == null) return null;

        return skillPlayer.getSkillInfoInventory().getInventory();
    }

    /**
     * @return The maximum level set or Integer.MAX_VALUE if unlimited levels is active
     */
    public int getMaxLevel() {
        return maxLevel;
    }


    /**
     * Performs a binary search on the collectiveXP array.
     * Looks for the fist element less than the given value
     * @param xp The total xp
     * @return The index which corresponds to the level between 0 and length-1
     */
    private int getLevelBinarySearch(long xp) {
        int low = 0;
        int high = collectiveXP.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (collectiveXP[mid] > xp) {
                high = mid - 1;
            }
            else if (collectiveXP[mid] < xp) {
                low = mid + 1;
            }
            else {
                return mid;
            }
        }

        return Math.max(high, 0);
    }
}
