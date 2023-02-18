package com.github.gavvydizzle.skillsplugin.utils;

import com.github.gavvydizzle.skillsplugin.configs.MessagesConfig;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

    // General Errors
    public static String playerNotFound, invalidSkillType;

    // Specific Errors
    public static String invalidSkillInventory;
    public static String invalidSelfSkillInventory;

    public static void reloadMessages() {
        FileConfiguration config = MessagesConfig.get();
        config.options().copyDefaults(true);

        // General Errors
        config.addDefault("playerNotFound", "&c{player_name} is not a valid player");
        config.addDefault("invalidSkillType", "&c{input} is not a valid skill type");

        // Specific Errors
        config.addDefault("invalidSkillInventory", "&c{player_name}'s skill menu could not be loaded");
        config.addDefault("invalidSelfSkillInventory", "&cYour skill inventory could not be loaded. If this issue persists after relogging please alert an admin");

        MessagesConfig.save();

        // General Errors
        playerNotFound = Colors.conv(config.getString("playerNotFound"));
        invalidSkillType = Colors.conv(config.getString("invalidSkillType"));

        // Specific Errors
        invalidSkillInventory = Colors.conv(config.getString("invalidSkillInventory"));
        invalidSelfSkillInventory = Colors.conv(config.getString("invalidSelfSkillInventory"));
    }

}
