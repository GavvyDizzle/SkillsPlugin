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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import javax.annotation.Nullable;
import java.util.*;

/**
 * HUNTER SKILL
 * Obtaining XP: Killing and breeding passive mobs
 * Passive Abilities:
 * - Chance for double drops
 * - Increase breeding experience
 * - Double wool from sheep chance
 * Active Ability: Killing a mob spawns 2 of itself (create a max duplication number per ability)
 */
public class Hunter extends Skill {

    private PassiveAbility doubleDrops, increasedBreedingXP, woolRegeneration;
    private String breedingXPMultiplierEquation;
    private ActiveAbility activeAbility;
    private String lengthTicksEquation;
    private final HashMap<EntityType, Integer> entityMap;
    private int breedingXP;
    private final ArrayList<UUID> activeAbilities;

    public Hunter(LevelManager levelManager) {
        super(SkillType.HUNTER, levelManager);
        entityMap = new HashMap<>();
        activeAbilities = new ArrayList<>();
        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.chance", "0.1*x+4");
        config.addDefault(getSkillType().name() + ".passive.increasedBreedingXP.minLevel", 10);
        config.addDefault(getSkillType().name() + ".passive.increasedBreedingXP.chance", 100);
        config.addDefault(getSkillType().name() + ".passive.increasedBreedingXP.multiplierEquation", "0.2*x");
        config.addDefault(getSkillType().name() + ".passive.woolRegeneration.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.woolRegeneration.chance", "0.2*x+30");
        config.addDefault(getSkillType().name() + ".active.minLevel", 20);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.lengthTicksEquation", "8*x+200");

        config.addDefault(getSkillType().name() + ".breedingXP.enabled", true);
        config.addDefault(getSkillType().name() + ".breedingXP.amount", 50);

        config.addDefault(getSkillType().name() + ".killXP.CHICKEN", 100);
        config.addDefault(getSkillType().name() + ".killXP.COW", 100);
        config.addDefault(getSkillType().name() + ".killXP.GOAT", 100);
        config.addDefault(getSkillType().name() + ".killXP.MUSHROOM_COW", 200);
        config.addDefault(getSkillType().name() + ".killXP.PIG", 100);
        config.addDefault(getSkillType().name() + ".killXP.RABBIT", 100);
        config.addDefault(getSkillType().name() + ".killXP.SHEEP", 100);
        config.addDefault(getSkillType().name() + ".killXP.TURTLE", 100);

        doubleDrops = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.doubleDrops.minLevel"),
                config.getString(getSkillType().name() + ".passive.doubleDrops.chance"));

        increasedBreedingXP = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.increasedBreedingXP.minLevel"),
                config.getString(getSkillType().name() + ".passive.increasedBreedingXP.chance"));
        breedingXPMultiplierEquation = config.getString(getSkillType().name() + ".passive.increasedBreedingXP.multiplierEquation");

        woolRegeneration = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.woolRegeneration.minLevel"),
                config.getString(getSkillType().name() + ".passive.woolRegeneration.chance"));

        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        lengthTicksEquation = config.getString(getSkillType().name() + ".active.lengthTicksEquation");

        breedingXP = config.getBoolean(getSkillType().name() + ".breedingXP.enabled") ? Math.max(0, config.getInt(getSkillType().name() + ".breedingXP.amount")) : 0;

        entityMap.clear();
        for (String key : Objects.requireNonNull(config.getConfigurationSection(getSkillType().name() + ".killXP")).getKeys(false)) {
            String path = getSkillType().name() + ".killXP." + key;

            try {
                EntityType entityType = EntityType.valueOf(key);
                int xp = Math.max(config.getInt(path), 0);
                entityMap.put(entityType, xp);
            }
            catch (Exception ignored) {
                SkillsPlugin.getInstance().getLogger().warning("The EntityType " + key + " could not be found. It is being ignored");
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null || !entityMap.containsKey(e.getEntity().getType())) return;

        Player killer = e.getEntity().getKiller();
        levelManager.givePlayerExperience(getSkillType(), killer, entityMap.get(e.getEntity().getType()));

        if (doubleDrops.shouldActivate(getPlayerLevel(killer))) {
            for (ItemStack itemStack : e.getDrops()) {
                itemStack.setAmount(itemStack.getAmount() * 2);
            }
        }

        if (activeAbilities.contains(killer.getUniqueId())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(), () -> {
                e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
                e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
            });
        }
    }

    // Breeding of any entity is valid
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBreed(EntityBreedEvent e) {
        if (breedingXP == 0) return;

        if (!(e.getBreeder() instanceof Player)) return;

        Player breeder = (Player) e.getBreeder();
        levelManager.givePlayerExperience(getSkillType(), breeder, breedingXP);

        int level = getPlayerLevel(breeder);
        if (increasedBreedingXP.shouldActivate(level)) {
            e.setExperience((int) (e.getExperience() * Numbers.eval(breedingXPMultiplierEquation.replace("x", "" + level))));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSheepShear(PlayerShearEntityEvent e) {
        if (e.getEntity().getType() != EntityType.SHEEP) return;

        if (woolRegeneration.shouldActivate(getPlayerLevel(e.getPlayer()))) {
            Sheep sheep = (Sheep) e.getEntity();
            Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(), () -> sheep.setSheared(false));
        }
    }


    @EventHandler (priority = EventPriority.MONITOR)
    private void onEntityClick(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                isValidTool(e.getPlayer().getInventory().getItemInMainHand().getType())) {
            Player player = e.getPlayer();

            if (activeAbility.canAbilityActivate(getPlayerLevel(player), levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) {
                Entity target = getClosestEntity(player);
                if (target == null) return;

                // The entity must be in the entityMap to be found by the getClosestEntity() method
                startActiveAbility(player);
            }
        }
    }

    private boolean isValidTool(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("_sword") || name.contains("_axe");
    }

    @Override
    public void startActiveAbility(Player player) {
        setActiveAbilityCooldown(player);
        activeAbilities.add(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPlugin.getInstance(),
                () -> activeAbilities.remove(player.getUniqueId()),
                (int) Numbers.eval(lengthTicksEquation.replace("x", "" + getPlayerLevel(player))));
    }

    /**
     * Gets the closest entity the player is looking at.
     * The Entity's type is guaranteed to be in the entityMap
     * @param player The player
     * @return The closest entity the player is looking at or null if a candidate is not found
     */
    @Nullable
    private Entity getClosestEntity(Player player) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5, entity -> entityMap.containsKey(entity.getType()));
        if (rayTraceResult == null) return null;
        return rayTraceResult.getHitEntity();
    }
}