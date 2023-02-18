package com.github.gavvydizzle.skillsplugin.skill.skills;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.ActiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.PassiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.Skill;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Objects;

/**
 * SLAYER SKILL
 * Obtaining XP: Killing entities with melee
 * Passive Abilities:
 * - Chance to give double mob drops
 * - Drop increased experience
 * Active Ability: Give yourself regeneration and resistance for a short time
 */
public class Slayer extends Skill {

    private PassiveAbility doubleDrops, increasedKillXP;
    private String xpMultiplierEquation;
    private ActiveAbility activeAbility;
    private String lengthTicksEquation;
    private int regenerationLevel, resistanceLevel;
    private final HashMap<EntityType, Integer> entityMap;

    public Slayer(LevelManager levelManager) {
        super(SkillType.SLAYER, levelManager);
        entityMap = new HashMap<>();
        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.doubleDrops.chance", "0.1*x+4");
        config.addDefault(getSkillType().name() + ".passive.increasedKillXP.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.increasedKillXP.chance", 100);
        config.addDefault(getSkillType().name() + ".passive.increasedKillXP.multiplierEquation", "0.01*x+0.9");
        config.addDefault(getSkillType().name() + ".active.minLevel", 20);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.lengthTicksEquation", "4*x+160");
        config.addDefault(getSkillType().name() + ".active.effects.regeneration.level", 1);
        config.addDefault(getSkillType().name() + ".active.effects.resistance.level", 1);

        config.addDefault(getSkillType().name() + ".killXP.BLAZE", 100);
        config.addDefault(getSkillType().name() + ".killXP.CAVE_SPIDER", 100);
        config.addDefault(getSkillType().name() + ".killXP.CREEPER", 100);
        config.addDefault(getSkillType().name() + ".killXP.DROWNED", 100);
        config.addDefault(getSkillType().name() + ".killXP.ENDER_DRAGON", 10000);
        config.addDefault(getSkillType().name() + ".killXP.ENDERMAN", 100);
        config.addDefault(getSkillType().name() + ".killXP.ENDERMITE", 100);
        config.addDefault(getSkillType().name() + ".killXP.EVOKER", 100);
        config.addDefault(getSkillType().name() + ".killXP.GHAST", 100);
        config.addDefault(getSkillType().name() + ".killXP.GUARDIAN", 100);
        config.addDefault(getSkillType().name() + ".killXP.HOGLIN", 200);
        config.addDefault(getSkillType().name() + ".killXP.HUSK", 100);
        config.addDefault(getSkillType().name() + ".killXP.MAGMA_CUBE", 100);
        config.addDefault(getSkillType().name() + ".killXP.PHANTOM", 200);
        config.addDefault(getSkillType().name() + ".killXP.PIGLIN", 100);
        config.addDefault(getSkillType().name() + ".killXP.PIGLIN_BRUTE", 500);
        config.addDefault(getSkillType().name() + ".killXP.PILLAGER", 100);
        config.addDefault(getSkillType().name() + ".killXP.RAVAGER", 500);
        config.addDefault(getSkillType().name() + ".killXP.SHULKER", 150);
        config.addDefault(getSkillType().name() + ".killXP.SILVERFISH", 100);
        config.addDefault(getSkillType().name() + ".killXP.SKELETON", 100);
        config.addDefault(getSkillType().name() + ".killXP.SLIME", 100);
        config.addDefault(getSkillType().name() + ".killXP.SPIDER", 100);
        config.addDefault(getSkillType().name() + ".killXP.STRAY", 100);
        config.addDefault(getSkillType().name() + ".killXP.VEX", 250);
        config.addDefault(getSkillType().name() + ".killXP.VINDICATOR", 100);
        config.addDefault(getSkillType().name() + ".killXP.WARDEN", 5000);
        config.addDefault(getSkillType().name() + ".killXP.WITCH", 100);
        config.addDefault(getSkillType().name() + ".killXP.WITHER", 5000);
        config.addDefault(getSkillType().name() + ".killXP.WITHER_SKELETON", 100);
        config.addDefault(getSkillType().name() + ".killXP.ZOGLIN", 200);
        config.addDefault(getSkillType().name() + ".killXP.ZOMBIE_VILLAGER", 100);
        config.addDefault(getSkillType().name() + ".killXP.ZOMBIFIED_PIGLIN", 100);

        doubleDrops = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.doubleDrops.minLevel"),
                config.getString(getSkillType().name() + ".passive.doubleDrops.chance"));

        increasedKillXP = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.increasedKillXP.minLevel"),
                config.getString(getSkillType().name() + ".passive.increasedKillXP.chance"));
        xpMultiplierEquation = config.getString(getSkillType().name() + ".passive.increasedKillXP.multiplierEquation");


        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        lengthTicksEquation = config.getString(getSkillType().name() + ".active.lengthTicksEquation");
        regenerationLevel = config.getInt(getSkillType().name() + ".active.effects.regeneration.level") - 1;
        resistanceLevel = config.getInt(getSkillType().name() + ".active.effects.resistance.level") - 1;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null || !entityMap.containsKey(e.getEntity().getType())) return;

        Player killer = e.getEntity().getKiller();
        levelManager.givePlayerExperience(getSkillType(), killer, entityMap.get(e.getEntity().getType()));

        int level = getPlayerLevel(killer);
        if (doubleDrops.shouldActivate(level)) {
            for (ItemStack itemStack : e.getDrops()) {
                itemStack.setAmount(itemStack.getAmount() * 2);
            }
        }

        if (increasedKillXP.shouldActivate(level)) {
            e.setDroppedExp((int) (e.getDroppedExp() * Numbers.eval(xpMultiplierEquation.replace("x", "" + level))));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onWeaponClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL && e.getPlayer().isSneaking() && isValidTool(e.getPlayer().getInventory().getItemInMainHand().getType())) { // Right/left click block/air
            Player player = e.getPlayer();

            if (activeAbility.canAbilityActivate(getPlayerLevel(player), levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) {
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

        int lengthTicks = (int) Numbers.eval(lengthTicksEquation.replace("x", "" + getPlayerLevel(player)));
        if (regenerationLevel >= 0) player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, lengthTicks, regenerationLevel));
        if (resistanceLevel >= 0) player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, lengthTicks, resistanceLevel));
    }
}