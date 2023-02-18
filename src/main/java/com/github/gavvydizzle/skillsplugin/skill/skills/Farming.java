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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * FARMING SKILL
 * Obtaining XP: Breaking full-grown crops and tilling land
 * Passive Abilities:
 * - Chance to double crop drops
 * - Chance for bone meal to spread to other crops
 * Active Ability: Replant crops when broken with hoe (no items consumed when planting)
 */
public class Farming extends Skill {

    private PassiveAbility doubleDrops, boneMealSpread;
    private ActiveAbility activeAbility;
    private String lengthTicksEquation;
    private final HashMap<Material, Integer> cropMap;
    private int tillXP;
    private final Vector[] offsets;
    private final ArrayList<UUID> activeAbilities;

    public Farming(LevelManager levelManager) {
        super(SkillType.FARMING, levelManager);
        cropMap = new HashMap<>();
        activeAbilities = new ArrayList<>();

        offsets = new Vector[8];
        offsets[0] = new Vector(-1, 0, -1);
        offsets[1] = new Vector(-1, 0, 0);
        offsets[2] = new Vector(-1, 0, 1);
        offsets[3] = new Vector(0, 0, 1);
        offsets[4] = new Vector(0, 0, -1);
        offsets[5] = new Vector(1, 0, -1);
        offsets[6] = new Vector(1, 0, 0);
        offsets[7] = new Vector(1, 0, 1);

        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.chance", "0.1*x+5");
        config.addDefault(getSkillType().name() + ".passive.boneMealSpread.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.boneMealSpread.chance", "0.2*x+5");
        config.addDefault(getSkillType().name() + ".active.minLevel", 20);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.lengthTicksEquation", "8*x+200");

        config.addDefault(getSkillType().name() + ".tillXP.enabled", true);
        config.addDefault(getSkillType().name() + ".tillXP.amount", 20);

        config.addDefault(getSkillType().name() + ".harvestXP.BEETROOTS", 100);
        config.addDefault(getSkillType().name() + ".harvestXP.CARROTS", 100);
        config.addDefault(getSkillType().name() + ".harvestXP.POTATOES", 100);
        config.addDefault(getSkillType().name() + ".harvestXP.WHEAT", 100);
        config.addDefault(getSkillType().name() + ".harvestXP.NETHER_WARTS", 150);

        doubleDrops = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.doubleDrops.minLevel"),
                config.getString(getSkillType().name() + ".passive.doubleDrops.chance"));

        boneMealSpread = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.boneMealSpread.minLevel"),
                config.getString(getSkillType().name() + ".passive.boneMealSpread.chance"));

        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        lengthTicksEquation = config.getString(getSkillType().name() + ".active.lengthTicksEquation");

        tillXP = config.getBoolean(getSkillType().name() + ".tillXP.enabled") ? Math.max(0, config.getInt(getSkillType().name() + ".tillXP.amount")) : 0;

        cropMap.clear();
        for (String key : Objects.requireNonNull(config.getConfigurationSection(getSkillType().name() + ".harvestXP")).getKeys(false)) {
            String path = getSkillType().name() + ".harvestXP." + key;

            try {
                Material material = Material.getMaterial(key);
                int xp = Math.max(config.getInt(path), 0);
                cropMap.put(material, xp);
            }
            catch (Exception ignored) {
                SkillsPlugin.getInstance().getLogger().warning("The Material " + key + " could not be found. It is being ignored");
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onLandTill(PlayerInteractEvent e) {
        if (tillXP == 0) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null &&
                (e.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("_hoe") ||
                e.getPlayer().getInventory().getItemInOffHand().getType().toString().toLowerCase().contains("_hoe"))) {

            Material material = e.getClickedBlock().getType();
            if (material == Material.DIRT || material == Material.GRASS_BLOCK || material == Material.DIRT_PATH) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(), () -> {
                    if (e.getClickedBlock().getType() == Material.FARMLAND) {
                        levelManager.givePlayerExperience(getSkillType(), e.getPlayer(), tillXP);
                    }
                }, 0);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onCropBreak(BlockBreakEvent e) {
        if (!cropMap.containsKey(e.getBlock().getType())) return;

        if (!(e.getBlock().getBlockData() instanceof Ageable)) return;
        Ageable crop = (Ageable) e.getBlock().getBlockData();
        if (crop.getAge() != crop.getMaximumAge()) return;

        levelManager.givePlayerExperience(getSkillType(), e.getPlayer(), cropMap.get(e.getBlock().getType()));

        int level = getPlayerLevel(e.getPlayer());
        if (doubleDrops.shouldActivate(level)) {
            for (ItemStack itemStack : e.getBlock().getDrops()) {
                itemStack.setAmount(itemStack.getAmount() * 2);
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), itemStack);
            }
            e.setDropItems(false);
        }

        if (activeAbilities.contains(e.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(), () -> {
                crop.setAge(0);
                e.getBlock().setBlockData(crop);
            }, 0);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBoneMealUse(BlockFertilizeEvent e) {
        if (!cropMap.containsKey(e.getBlock().getType())) return;
        if (e.getPlayer() == null) return;

        int level = getPlayerLevel(e.getPlayer());
        if (boneMealSpread.shouldActivate(level)) {
            for (Vector vector : offsets) {
                Block relative = e.getBlock().getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
                if (cropMap.containsKey(relative.getType())) {
                    relative.applyBoneMeal(BlockFace.DOWN);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onHoeClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();

            if (!activeAbility.canAbilityActivate(getPlayerLevel(player), levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) return;

            if (!e.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("_hoe")) return;

            startActiveAbility(player);
        }
    }

    @Override
    public void startActiveAbility(Player player) {
        setActiveAbilityCooldown(player);
        activeAbilities.add(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(),
                () -> activeAbilities.remove(player.getUniqueId()),
                (int) Numbers.eval(lengthTicksEquation.replace("x", "" + getPlayerLevel(player))));
    }
}