package com.github.gavvydizzle.skillsplugin.skill.skills;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.ActiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.PassiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.Skill;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.gavvydizzle.skillsplugin.skill.skills.helper.SpinCycle;
import com.github.gavvydizzle.skillsplugin.utils.Sounds;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * MINING SKILL
 * Obtaining XP: Mining certain blocks with a pickaxe
 * Passive Abilities:
 * - Chance for double ore drops (without silk touch)
 * - Chance for super fortune (spin cycle enchant)
 * Active Ability: High level of haste for a short time (hasten enchant)
 */
public class Mining extends Skill {

    private boolean allowSilkTouchForXP, allowSilkTouchForRegenerate, allowSilkTouchForSpinCycle;
    private PassiveAbility regenerate, spinCycle;
    private final Set<Material> regenerateOreList;
    private ActiveAbility activeAbility;
    private String lengthTicksEquation;
    private int hasteLevel;
    private final Map<Material, Integer> experienceBlocks, spinCycleOres;
    private int defaultBlockXP;

    private final List<String> doubleDropsList;

    public Mining(LevelManager levelManager) {
        super(SkillType.MINING, levelManager);
        experienceBlocks = new HashMap<>();
        spinCycleOres = new HashMap<>();
        regenerateOreList = new HashSet<>();

        doubleDropsList = Arrays.asList(
                Material.NETHER_QUARTZ_ORE.name(),
                Material.NETHER_GOLD_ORE.name(),
                Material.COAL_ORE.name(),
                Material.REDSTONE_ORE.name(),
                Material.LAPIS_ORE.name(),
                Material.COPPER_ORE.name(),
                Material.IRON_ORE.name(),
                Material.GOLD_ORE.name(),
                Material.DIAMOND_ORE.name(),
                Material.EMERALD_ORE.name(),
                Material.DEEPSLATE_COAL_ORE.name(),
                Material.DEEPSLATE_REDSTONE_ORE.name(),
                Material.DEEPSLATE_LAPIS_ORE.name(),
                Material.DEEPSLATE_COPPER_ORE.name(),
                Material.DEEPSLATE_IRON_ORE.name(),
                Material.DEEPSLATE_GOLD_ORE.name(),
                Material.DEEPSLATE_DIAMOND_ORE.name(),
                Material.DEEPSLATE_EMERALD_ORE.name()
        );

        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".xp.default", 0);
        config.addDefault(getSkillType().name() + ".xp.allowSilkTouch", false);
        config.addDefault(getSkillType().name() + ".xp.blockList", new HashMap<>());
        config.addDefault(getSkillType().name() + ".passive.regenerate.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.regenerate.chance", "0.1*x+4");
        config.addDefault(getSkillType().name() + ".passive.regenerate.allowSilkTouch", false);
        config.addDefault(getSkillType().name() + ".passive.regenerate.blockList", doubleDropsList);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.chance", "0.03*x+1");
        config.addDefault(getSkillType().name() + ".passive.spinCycle.allowSilkTouch", false);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList", new HashMap<>());
        config.addDefault(getSkillType().name() + ".active.minLevel", 20);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.lengthTicksEquation", "2*x+80");
        config.addDefault(getSkillType().name() + ".active.effects.haste.level", 120);

        config.addDefault(getSkillType().name() + ".xp.blockList.QUARTZ_ORE", 150);
        config.addDefault(getSkillType().name() + ".xp.blockList.NETHER_GOLD_ORE", 200);
        config.addDefault(getSkillType().name() + ".xp.blockList.COAL_ORE", 100);
        config.addDefault(getSkillType().name() + ".xp.blockList.REDSTONE_ORE", 200);
        config.addDefault(getSkillType().name() + ".xp.blockList.LAPIS_ORE", 200);
        config.addDefault(getSkillType().name() + ".xp.blockList.COPPER_ORE", 150);
        config.addDefault(getSkillType().name() + ".xp.blockList.IRON_ORE", 150);
        config.addDefault(getSkillType().name() + ".xp.blockList.GOLD_ORE", 250);
        config.addDefault(getSkillType().name() + ".xp.blockList.DIAMOND_ORE", 750);
        config.addDefault(getSkillType().name() + ".xp.blockList.EMERALD_ORE", 1000);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_COAL_ORE", 5000);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_REDSTONE_ORE", 200);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_LAPIS_ORE", 200);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_COPPER_ORE", 150);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_IRON_ORE", 150);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_GOLD_ORE", 250);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_DIAMOND_ORE", 750);
        config.addDefault(getSkillType().name() + ".xp.blockList.DEEPSLATE_EMERALD_ORE", 10000);

        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.QUARTZ_ORE", 32);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.NETHER_GOLD_ORE", 64);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.COAL_ORE", 14);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.REDSTONE_ORE", 36);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.LAPIS_ORE", 48);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.COPPER_ORE", 16);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.IRON_ORE", 12);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.GOLD_ORE", 10);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DIAMOND_ORE", 8);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.EMERALD_ORE", 16);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_COAL_ORE", 14);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_REDSTONE_ORE", 36);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_LAPIS_ORE", 48);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_COPPER_ORE", 16);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_IRON_ORE", 12);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_GOLD_ORE", 10);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_DIAMOND_ORE", 8);
        config.addDefault(getSkillType().name() + ".passive.spinCycle.blockList.DEEPSLATE_EMERALD_ORE", 16);

        defaultBlockXP = config.getInt(getSkillType().name() + ".xp.default");
        allowSilkTouchForXP = config.getBoolean(getSkillType().name() + ".xp.allowSilkTouch");
        experienceBlocks.clear();
        if (config.getConfigurationSection(getSkillType().name() + ".xp.blockList") != null) {
            for (String material : Objects.requireNonNull(config.getConfigurationSection(getSkillType().name() + ".xp.blockList")).getKeys(false)) {
                String path = getSkillType().name() + ".xp.blockList." + material;
                try {
                    experienceBlocks.put(Material.getMaterial(material), config.getInt(path));
                } catch (Exception e) {
                    SkillsPlugin.getInstance().getLogger().warning("The Material " + material + " could not be found. It is being ignored");
                }
            }
        }

        regenerate = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.regenerate.minLevel"),
                config.getString(getSkillType().name() + ".passive.regenerate.chance"));
        allowSilkTouchForRegenerate = config.getBoolean(getSkillType().name() + ".passive.regenerate.allowSilkTouch");
        regenerateOreList.clear();
        for (String material : config.getStringList(getSkillType().name() + ".passive.regenerate.blockList")) {
            try {
                regenerateOreList.add(Material.getMaterial(material));
            }
            catch (Exception e) {
                SkillsPlugin.getInstance().getLogger().warning("The Material " + material + " could not be found. It is being ignored");
            }
        }

        spinCycle = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.spinCycle.minLevel"),
                config.getString(getSkillType().name() + ".passive.spinCycle.chance"));
        allowSilkTouchForSpinCycle = config.getBoolean(getSkillType().name() + ".passive.spinCycle.allowSilkTouch");
        spinCycleOres.clear();
        if (config.getConfigurationSection(getSkillType().name() + ".passive.spinCycle.blockList") != null) {
            for (String material : Objects.requireNonNull(config.getConfigurationSection(getSkillType().name() + ".passive.spinCycle.blockList")).getKeys(false)) {
                String path = getSkillType().name() + ".passive.spinCycle.blockList." + material;
                try {
                    spinCycleOres.put(Material.getMaterial(material), config.getInt(path));
                } catch (Exception e) {
                    SkillsPlugin.getInstance().getLogger().warning("The Material " + material + " could not be found. It is being ignored");
                }
            }
        }

        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        lengthTicksEquation = config.getString(getSkillType().name() + ".active.lengthTicksEquation");
        hasteLevel = config.getInt(getSkillType().name() + ".active.effects.haste.level") - 1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPickaxeBlockBreak(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (!isValidTool(item.getType())) return;

        Player player = e.getPlayer();
        int level = getPlayerLevel(player);
        Material material = e.getBlock().getType();
        boolean hasSilkTouch = hasSilkTouchEnchant(item);

        if ((allowSilkTouchForXP || !hasSilkTouch) && experienceBlocks.containsKey(material)) {
            int xp = experienceBlocks.containsKey(material) ? experienceBlocks.get(material) : defaultBlockXP;
            if (xp > 0) {
                levelManager.givePlayerExperience(getSkillType(), player, xp);
            }
        }

        if ((allowSilkTouchForRegenerate || !hasSilkTouch) && regenerateOreList.contains(material) && regenerate.shouldActivate(level)) {
            e.getBlock().getWorld().spawnParticle(Particle.SPELL, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 4);

            Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(), () ->  {
                e.getBlock().setType(material);
                Sounds.regenerateSound.playSound(e.getBlock().getLocation());
            }, 0);
        }

        if ((allowSilkTouchForSpinCycle || !hasSilkTouch) && spinCycleOres.containsKey(material) && spinCycle.shouldActivate(level)) {
            SpinCycle.createSpinCycle(player, e.getBlock(), hasSilkTouch, spinCycleOres.get(material));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onWeaponClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL && e.getPlayer().isSneaking() && isValidTool(e.getPlayer().getInventory().getItemInMainHand().getType())) { // Right/left click block/air
            Player player = e.getPlayer();

            if (activeAbility.canAbilityActivate(getPlayerLevel(player), levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) {
                startActiveAbility(player);
            }
        }
    }

    private boolean isValidTool(Material material) {
        return material.name().toLowerCase().contains("_pickaxe");
    }

    private boolean hasSilkTouchEnchant(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
    }

    @Override
    public void startActiveAbility(Player player) {
        setActiveAbilityCooldown(player);

        int lengthTicks = (int) Numbers.eval(lengthTicksEquation.replace("x", "" + getPlayerLevel(player)));
        if (hasteLevel >= 0) player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, lengthTicks, hasteLevel));
    }
}