package com.github.gavvydizzle.skillsplugin.gui;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.GUIConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.player.SkillPlayer;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.gavvydizzle.skillsplugin.utils.Pair;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class SkillInfoInventory {

    private static String inventoryName;
    private static int inventorySize;
    private static ItemStack[] fillerContents;
    private static ArrayList<InventorySkillItem> inventoryItems;

    private final SkillPlayer skillPlayer;

    public SkillInfoInventory(SkillPlayer skillPlayer) {
        this.skillPlayer = skillPlayer;
    }

    public static void reload() {
        FileConfiguration config = GUIConfig.get();
        config.options().copyDefaults(true);
        config.addDefault("name", "{owner} Skill Levels");
        config.addDefault("rows", 3);
        config.addDefault("filler", "gray");
        for (SkillType skillType : SkillType.values()) {
            InventorySkillItem.addDefaults(config, "skillItems." + skillType.name());
        }

        inventoryName = Colors.conv(config.getString("name"));
        inventorySize = Numbers.constrain(config.getInt("rows") * 9, 9, 54);
        ItemStack filler = ColoredItems.getGlassByName(config.getString("filler"));
        fillerContents = new ItemStack[inventorySize];
        for (int i = 0; i < inventorySize; i++) {
            fillerContents[i] = filler;
        }

        inventoryItems = new ArrayList<>(SkillType.values().length);
        for (SkillType skillType : SkillType.values()) {
            inventoryItems.add(new InventorySkillItem(config, "skillItems." + skillType.name(), skillType));
        }
    }

    /**
     * Gets the inventory with information about all of a player's stata
     * @return An inventory
     */
    @NotNull
    public Inventory getInventory() {
        // Inventory cannot be cached since the items change often
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName.replace("{owner}", skillPlayer.getPlayer().getName()));
        inventory.setContents(fillerContents);

        for (InventorySkillItem inventorySkillItem : inventoryItems) {
            if (!Numbers.isWithinRange(inventorySkillItem.getSlot(), 0, inventorySize-1)) continue;

            ItemStack item = inventorySkillItem.getItemStack().clone();
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            Pair<String, ArrayList<String>> pair = getWithPlaceholders(inventorySkillItem.getSkillType(), meta.getDisplayName(), meta.getLore());
            meta.setDisplayName(pair.first);
            meta.setLore(pair.second);
            item.setItemMeta(meta);
            inventory.setItem(inventorySkillItem.getSlot(), item);
        }

        return inventory;
    }

    //*** SKILL PLACEHOLDERS ***//
    // {skill} - skill name all lowercase
    // {skill_capitalized} - skill name with first letter capitalized
    // {level} - current player level
    // {max_level} - max level (or Integer.MAX_INT if unlimited levels)
    // {current_level_xp} - XP towards next level
    // {xp_remaining} - XP remaining until next level
    // {total_xp} - Total XP
    // {progress} - Percent towards next level
    // {progress_bar} - Visual progress bar
    private Pair<String,ArrayList<String>> getWithPlaceholders(SkillType skillType, String displayName, @Nullable List<String> arr) {
        LevelManager levelManager = SkillsPlugin.getInstance().getLevelManager();

        int level = skillPlayer.getSkillInformation().getLevel(skillType);
        int maxLevel = levelManager.getMaxLevel();
        long totalXP = skillPlayer.getSkillInformation().getTotalXP(skillType);
        String currentLevelXP = Numbers.withSuffix(levelManager.getXPTowardsNextLevel(totalXP));
        String xpRemaining = Numbers.withSuffix(levelManager.getXPUntilNextLevel(totalXP));
        String totalXPString = Numbers.withSuffix(levelManager.getXPUntilNextLevel(totalXP));
        double progress = levelManager.getProgress(totalXP);
        double progressRounded = Numbers.round(progress * 100, 2);
        String progressBar = getProgressBar(progress * 100);

        String name = displayName.replace("{skill}", skillType.lowercaseName)
                .replace("{skill_capitalized}", skillType.uppercaseName)
                .replace("{level}", "" + level)
                .replace("{max_level}", "" + maxLevel)
                .replace("{current_level_xp}", currentLevelXP)
                .replace("{xp_remaining}", xpRemaining)
                .replace("{total_xp}", totalXPString)
                .replace("{progress}", "" + progressRounded)
                .replace("{progress_bar}", progressBar);

        ArrayList<String> lore = new ArrayList<>();
        if (arr != null) {
            for (String str : arr) {
                if (!str.contains("{")) {
                    lore.add(str);
                    continue;
                }

                lore.add(str.replace("{skill}", skillType.lowercaseName)
                        .replace("{skill_capitalized}", skillType.uppercaseName)
                        .replace("{level}", "" + level)
                        .replace("{max_level}", "" + maxLevel)
                        .replace("{current_level_xp}", currentLevelXP)
                        .replace("{xp_remaining}", xpRemaining)
                        .replace("{total_xp}", totalXPString)
                        .replace("{progress}", "" + progressRounded)
                        .replace("{progress_bar}", progressBar)
                );
            }
        }
        return new Pair<>(name, lore);
    }

    private String getProgressBar(double percent) {
        String y = ChatColor.GREEN + "" + ChatColor.BOLD + ":";
        String n = ChatColor.GRAY + "" + ChatColor.BOLD + ":";
        String end = ChatColor.DARK_GRAY + "]";

        StringBuilder xpLine = new StringBuilder(ChatColor.DARK_GRAY + "[");
        int a = (int) (percent / 4);
        for (int i = 1; i <= 25; i++) {
            if (i <= a) {
                xpLine.append(y);
            }
            else {
                xpLine.append(n);
            }
        }
        xpLine.append(end);

        return xpLine.toString();
    }
}
