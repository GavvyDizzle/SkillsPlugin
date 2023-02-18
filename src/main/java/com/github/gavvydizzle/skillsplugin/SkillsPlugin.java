package com.github.gavvydizzle.skillsplugin;

import com.github.gavvydizzle.skillsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.skillsplugin.commands.PlayerCommandManager;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.gui.InventoryManager;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.skills.helper.SpinCycle;
import com.github.gavvydizzle.skillsplugin.papi.MyExpansion;
import com.github.gavvydizzle.skillsplugin.skill.SkillManager;
import com.github.gavvydizzle.skillsplugin.skill.skills.*;
import com.github.gavvydizzle.skillsplugin.storage.Database;
import com.github.gavvydizzle.skillsplugin.storage.SQLite;
import com.github.gavvydizzle.skillsplugin.utils.Messages;
import com.github.gavvydizzle.skillsplugin.utils.Sounds;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.Arrays;
import java.util.Objects;

public final class SkillsPlugin extends JavaPlugin {

    // TODO
    // Excavation Skill
    // Fishing Skill
    // Level up visuals
    // Bossbar visuals
    // Autosaving

    private static SkillsPlugin instance;
    private static Permission perms = null;
    private LevelManager levelManager;
    private InventoryManager inventoryManager;
    private SkillManager skillManager;
    private PlayerCommandManager playerCommandManager;
    private AdminCommandManager adminCommandManager;

    @Override
    public void onEnable() {
        setupPermissions();

        generateConfigHeader();
        saveDefaultConfig();

        instance = this;
        Database database = new SQLite(this);
        database.createTables();

        levelManager = new LevelManager(database);
        inventoryManager = new InventoryManager(levelManager);
        skillManager = new SkillManager();
        registerSkills();

        getServer().getPluginManager().registerEvents(levelManager, this);
        getServer().getPluginManager().registerEvents(inventoryManager, this);

        try {
            playerCommandManager = new PlayerCommandManager(Objects.requireNonNull(getCommand("skill")), inventoryManager);
        } catch (NullPointerException e) {
            getLogger().severe("The admin command name was changed in the plugin.yml file. Please make it \"skill\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            adminCommandManager = new AdminCommandManager(Objects.requireNonNull(getCommand("skillAdmin")), levelManager);
        } catch (NullPointerException e) {
            getLogger().severe("The admin command name was changed in the plugin.yml file. Please make it \"skillAdmin\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Messages.reloadMessages();
        Sounds.reload();

        try {
            new MyExpansion().register();
        }
        catch (Exception e) {
            getLogger().info("PlaceholderAPI hook not enabled");
        }
    }

    @Override
    public void onDisable() {
        if (levelManager != null) {
            levelManager.savePlayersOnShutdown();
        }

        if (SpinCycle.getArmorStands() != null) {
            for (ArmorStand armorStand : SpinCycle.getArmorStands()) {
                armorStand.remove();
            }
        }
    }

    private void generateConfigHeader() {
        getConfig().options().setHeader(Arrays.asList(
                "______________________________________________________",
                "   _____ __   _ ____     ____  __            _",
                "  / ___// /__(_) / /____/ __ \\/ /_  ______ _(_)___ ",
                "  \\__ \\/ //_/ / / / ___/ /_/ / / / / / __ `/ / __ \\",
                " ___/ / ,< / / / (__  ) ____/ / /_/ / /_/ / / / / /",
                "/____/_/|_/_/_/_/____/_/   /_/\\__,_/\\__, /_/_/ /_/ ",
                "                                   /____/",
                "Author: GavvyDizzle",
                "______________________________________________________"));
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        assert rsp != null;
        perms = rsp.getProvider();
    }

    private void registerSkills() {
        Archery archery = new Archery(levelManager);
        Excavation excavation = new Excavation(levelManager);
        Farming farming = new Farming(levelManager);
        Fishing fishing = new Fishing(levelManager);
        Foraging foraging = new Foraging(levelManager);
        Hunter hunter = new Hunter(levelManager);
        Mining mining = new Mining(levelManager);
        Slayer slayer = new Slayer(levelManager);
        AbilitiesConfig.save();

        getServer().getPluginManager().registerEvents(archery, this);
        getServer().getPluginManager().registerEvents(excavation, this);
        getServer().getPluginManager().registerEvents(farming, this);
        getServer().getPluginManager().registerEvents(fishing, this);
        getServer().getPluginManager().registerEvents(foraging, this);
        getServer().getPluginManager().registerEvents(hunter, this);
        getServer().getPluginManager().registerEvents(mining, this);
        getServer().getPluginManager().registerEvents(slayer, this);

        skillManager.registerSkill(archery);
        skillManager.registerSkill(excavation);
        skillManager.registerSkill(farming);
        skillManager.registerSkill(fishing);
        skillManager.registerSkill(foraging);
        skillManager.registerSkill(hunter);
        skillManager.registerSkill(mining);
        skillManager.registerSkill(slayer);
    }

    public static SkillsPlugin getInstance() {
        return instance;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public PlayerCommandManager getPlayerCommandManager() {
        return playerCommandManager;
    }

    public AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }
}
