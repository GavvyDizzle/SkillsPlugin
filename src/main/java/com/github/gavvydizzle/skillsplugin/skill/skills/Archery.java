package com.github.gavvydizzle.skillsplugin.skill.skills;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.AbilitiesConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.ActiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.PassiveAbility;
import com.github.gavvydizzle.skillsplugin.skill.Skill;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.RepeatingTask;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Objects;

/**
 * ARCHERY SKILL
 * Obtaining XP: Killing entities with arrow
 * Passive Abilities:
 * - Chance to add harming to arrow
 * - Chance to critical hit target (double damage)
 * Active Ability: Shoot a string of arrows (no durability lost, amount scales with level)
 */
public class Archery extends Skill {

    private PassiveAbility arrowHarming, criticalHit;
    private ActiveAbility activeAbility;
    private int harmingLevel, arrowSpreadTicks;
    private String criticalHitDamageEquation, numArrowsEquation;
    private final HashMap<EntityType, Integer> entityMap;

    public Archery(LevelManager levelManager) {
        super(SkillType.ARCHERY, levelManager);
        entityMap = new HashMap<>();
        reload();
    }

    @Override
    public void reload() {
        FileConfiguration config = AbilitiesConfig.get();
        config.addDefault(getSkillType().name() + ".passive.harming.minLevel", 5);
        config.addDefault(getSkillType().name() + ".passive.harming.chance", "1.4*sqrt(x)");
        config.addDefault(getSkillType().name() + ".passive.harming.harmingLevel", 1);
        config.addDefault(getSkillType().name() + ".passive.critical.minLevel", 15);
        config.addDefault(getSkillType().name() + ".passive.critical.chance", "5");
        config.addDefault(getSkillType().name() + ".passive.critical.damageEquation", "0.15*sqrt(x)+1");
        config.addDefault(getSkillType().name() + ".active.minLevel", 25);
        config.addDefault(getSkillType().name() + ".active.cooldownSeconds", 5);
        config.addDefault(getSkillType().name() + ".active.numArrowsEquation", 5);
        config.addDefault(getSkillType().name() + ".active.arrowSpreadTicks", 2);

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

        config.addDefault(getSkillType().name() + ".killXP.CHICKEN", 50);
        config.addDefault(getSkillType().name() + ".killXP.COW", 50);
        config.addDefault(getSkillType().name() + ".killXP.FOX", 50);
        config.addDefault(getSkillType().name() + ".killXP.FROG", 50);
        config.addDefault(getSkillType().name() + ".killXP.GOAT", 50);
        config.addDefault(getSkillType().name() + ".killXP.HORSE", 50);
        config.addDefault(getSkillType().name() + ".killXP.IRON_GOLEM", 100);
        config.addDefault(getSkillType().name() + ".killXP.LLAMA", 50);
        config.addDefault(getSkillType().name() + ".killXP.MULE", 50);
        config.addDefault(getSkillType().name() + ".killXP.MUSHROOM_COW", 50);
        config.addDefault(getSkillType().name() + ".killXP.OCELOT", 50);
        config.addDefault(getSkillType().name() + ".killXP.PANDA", 50);
        config.addDefault(getSkillType().name() + ".killXP.PARROT", 50);
        config.addDefault(getSkillType().name() + ".killXP.PIG", 50);
        config.addDefault(getSkillType().name() + ".killXP.POLAR_BEAR", 50);
        config.addDefault(getSkillType().name() + ".killXP.RABBIT", 50);
        config.addDefault(getSkillType().name() + ".killXP.SHEEP", 50);
        config.addDefault(getSkillType().name() + ".killXP.SKELETON_HORSE", 50);
        config.addDefault(getSkillType().name() + ".killXP.STRIDER", 50);
        config.addDefault(getSkillType().name() + ".killXP.TRADER_LLAMA", 200);
        config.addDefault(getSkillType().name() + ".killXP.TURTLE", 50);
        config.addDefault(getSkillType().name() + ".killXP.WANDERING_TRADER", 200);

        arrowHarming = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.harming.minLevel"),
                config.getString(getSkillType().name() + ".passive.harming.chance"));
        harmingLevel = Math.max(0, config.getInt(getSkillType().name() + ".passive.harming.harmingLevel") - 1);

        criticalHit = new PassiveAbility(config.getInt(getSkillType().name() + ".passive.critical.minLevel"),
                config.getString(getSkillType().name() + ".passive.critical.chance"));
        criticalHitDamageEquation = config.getString(getSkillType().name() + ".passive.critical.damageEquation");

        activeAbility = new ActiveAbility(config.getInt(getSkillType().name() + ".active.minLevel"),
                config.getInt(getSkillType().name() + ".active.cooldownSeconds"));
        numArrowsEquation = config.getString(getSkillType().name() + ".active.numArrowsEquation");
        arrowSpreadTicks = config.getInt(getSkillType().name() + ".active.arrowSpreadTicks");

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

    @EventHandler
    private void onEntityKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        // If the last thing to damage this entity was an arrow
        if (e.getEntity().getLastDamageCause() != null || e.getEntity().getLastDamageCause().getEntityType() != EntityType.ARROW) return;

        if (entityMap.containsKey(e.getEntity().getType())) {
            Player killer = e.getEntity().getKiller();
            levelManager.givePlayerExperience(getSkillType(), killer, entityMap.get(e.getEntity().getType()));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onArrowShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getProjectile() instanceof Arrow)) return; // Only allow arrows to be modified

        Arrow arrow = (Arrow) e.getProjectile();
        Player player = (Player) e.getEntity();
        int level = levelManager.getCachedLevel(getSkillType(), player);

        if (arrowHarming.shouldActivate(level)) {
            arrow.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, harmingLevel), true);
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        }

        if (criticalHit.shouldActivate(level)) {
            arrow.setDamage(arrow.getDamage() * Numbers.eval(criticalHitDamageEquation.replace("x", "" + level)));
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onBowClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            Player player = e.getPlayer();
            int level = getPlayerLevel(player);

            if (!activeAbility.canAbilityActivate(level, levelManager.getTimeSinceLastActiveAbilityUse(getSkillType(), player))) return;

            ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
            ItemStack offhandItem = e.getPlayer().getInventory().getItemInOffHand();

            if (mainHandItem.getType() == Material.BOW || mainHandItem.getType() == Material.CROSSBOW ||
            offhandItem.getType() == Material.BOW || offhandItem.getType() == Material.CROSSBOW) {

                setActiveAbilityCooldown(player);

                new RepeatingTask(SkillsPlugin.getInstance(), 0, arrowSpreadTicks) {
                    final int numArrows = (int) Numbers.eval(numArrowsEquation.replace("x", "" + level));
                    int count = 0;

                    @Override
                    public void run() {
                        if (numArrows <= count) {
                            cancel();
                            return;
                        }
                        count++;

                        Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation().subtract(0, 0.1, 0),
                                player.getEyeLocation().getDirection(), 3.0f, 0.6f, Arrow.class);
                        arrow.setShooter(player);
                        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

                        // Allow passive abilities to be applied to active ability arrows
                        if (arrowHarming.shouldActivate(level)) {
                            arrow.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, harmingLevel), true);
                        }

                        if (criticalHit.shouldActivate(level)) {
                            arrow.setDamage(arrow.getDamage() * Numbers.eval(criticalHitDamageEquation.replace("x", "" + level)));
                        }
                    }
                };
            }
        }
    }

    @Override
    public void startActiveAbility(Player player) {
        // Not needed for this skill
    }
}