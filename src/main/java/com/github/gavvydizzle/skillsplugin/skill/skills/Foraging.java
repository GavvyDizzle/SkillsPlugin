package com.github.gavvydizzle.skillsplugin.skill.skills;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.ActiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.PassiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.Skill;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * FORAGING SKILL
 * Obtaining XP: Mining trees
 * Passive Abilities:
 * - Chance to drop double wood
 * - Chance for apples dropped by leaves to turn into gapples
 * Active Ability: Instantly break entire trees for a short time
 */
public class Foraging extends Skill {

    private PassiveAbility doubleDrops, appleUpgrade;
    private int goldenAppleWeight, totalWeight;
    private ActiveAbility activeAbility;
    private String lengthTicksEquation, activeTempPermission;
    private final HashMap<Material, Integer> materialMap;

    public Foraging(LevelManager levelManager) {
        super(SkillType.FORAGING, levelManager);
        materialMap = new HashMap<>();
        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.chance", "0.1*x+4");
        config.addDefault(getSkillType().name() + ".passive.appleUpgrade.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.appleUpgrade.chance", "0.2*x+5");
        config.addDefault(getSkillType().name() + ".passive.appleUpgrade.weight.golden_apple", 199);
        config.addDefault(getSkillType().name() + ".passive.appleUpgrade.weight.enchanted_golden_apple", 1);
        config.addDefault(getSkillType().name() + ".active.minLevel", 20);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.lengthTicksEquation", "8x+200");
        config.addDefault(getSkillType().name() + ".active.tempPermission", "smoothtimber.use");

        config.addDefault(getSkillType().name() + ".breakXP.ACACIA_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.BIRCH_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.DARK_OAK_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.JUNGLE_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.MANGROVE_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.OAK_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.SPRUCE_LOG", 100);
        config.addDefault(getSkillType().name() + ".breakXP.CRIMSON_STEM", 100);
        config.addDefault(getSkillType().name() + ".breakXP.WARPED_STEM", 100);

        config.addDefault(getSkillType().name() + ".breakXP.ACACIA_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.AZALEA_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.FLOWERING_AZALEA_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.BIRCH_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.DARK_OAK_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.JUNGLE_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.MANGROVE_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.OAK_LEAVES", 10);
        config.addDefault(getSkillType().name() + ".breakXP.SPRUCE_LEAVES", 10);

        doubleDrops = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.doubleDrops.minLevel"),
                config.getString(getSkillType().name() + ".passive.doubleDrops.chance"));

        appleUpgrade = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.appleUpgrade.minLevel"),
                config.getString(getSkillType().name() + ".passive.appleUpgrade.chance"));
        goldenAppleWeight = config.getInt(getSkillType().name() + ".passive.appleUpgrade.weight.golden_apple");
        totalWeight = goldenAppleWeight + config.getInt(getSkillType().name() + ".passive.appleUpgrade.weight.enchanted_golden_apple");

        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        lengthTicksEquation = config.getString(getSkillType().name() + ".active.lengthTicksEquation");
        activeTempPermission = config.getString(getSkillType().name() + ".active.tempPermission");

        materialMap.clear();
        for (String key : Objects.requireNonNull(config.getConfigurationSection(getSkillType().name() + ".breakXP")).getKeys(false)) {
            String path = getSkillType().name() + ".breakXP." + key;

            try {
                Material material = Material.getMaterial(key);
                int xp = Math.max(config.getInt(path), 0);
                materialMap.put(material, xp);
            }
            catch (Exception ignored) {
                SkillsPlugin.getInstance().getLogger().warning("The Material " + key + " could not be found. It is being ignored");
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent e) {
        if (!materialMap.containsKey(e.getBlock().getType())) return;

        levelManager.givePlayerExperience(getSkillType(), e.getPlayer(), materialMap.get(e.getBlock().getType()));

        int level = getPlayerLevel(e.getPlayer());
        Collection<ItemStack> drops = e.getBlock().getDrops();

        if (e.getBlock().getType().name().toLowerCase().contains("_leaves") && appleUpgrade.shouldActivate(level)) {
            for (ItemStack itemStack : drops) {
                if (itemStack.getType() == Material.APPLE) {

                    itemStack.setAmount(1);

                    if (isUpgradedAppleGoldenApple()) {
                        itemStack.setType(Material.GOLDEN_APPLE);
                    }
                    else {
                        itemStack.setType(Material.ENCHANTED_GOLDEN_APPLE);
                    }

                    e.setDropItems(false);
                    break;
                }
            }
        }

        // Allows for double drops to work with apple upgrade
        if (doubleDrops.shouldActivate(level)) {
            for (ItemStack itemStack : drops) {
                itemStack.setAmount(itemStack.getAmount() * 2);
                e.getPlayer().sendMessage("Double drops");
            }
            e.setDropItems(false);
        }

        if (!e.isDropItems()) {
            for (ItemStack itemStack : drops) {
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), itemStack);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onAxeClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && isValidLog(e.getClickedBlock().getType())) {
            Player player = e.getPlayer();

            if (!e.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("_axe")) return;

            if (activeAbility.canAbilityActivate(getPlayerLevel(player), levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) {
                startActiveAbility(player);
            }
        }
    }

    private boolean isValidLog(Material material) {
        return material.name().toLowerCase().contains("_log") ||
                material == Material.CRIMSON_STEM ||
                material == Material.WARPED_STEM;
    }

    @Override
    public void startActiveAbility(Player player) {
        setActiveAbilityCooldown(player);

        SkillsPlugin.getPermissions().playerAdd(player, activeTempPermission);
        final OfflinePlayer offlinePlayer = player;

        Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(),
                () -> SkillsPlugin.getPermissions().playerRemove(null, offlinePlayer, activeTempPermission),
                (int) Numbers.eval(lengthTicksEquation.replace("x", "" + getPlayerLevel(player))));
    }

    private boolean isUpgradedAppleGoldenApple() {
        return Numbers.percentChance(goldenAppleWeight * 100.0/totalWeight);
    }
}