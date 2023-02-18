package com.github.gavvydizzle.skillsplugin.skill.skills.helper;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.utils.Sounds;
import com.github.mittenmc.serverutils.RepeatingTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;

public class SpinCycle {

    private static final ArrayList<ArmorStand> armorStands = new ArrayList<>();

    /**
     * Creates a spin cycle for this player
     * @param player The player
     * @param block The block
     * @param hasSilkTouch If the player's tool has silk touch
     * @param dropTotal The total number of items to drop
     */
    public static void createSpinCycle(Player player, Block block, boolean hasSilkTouch, int dropTotal) {
        Material material = block.getType();
        ItemStack nonSilkDrop = (ItemStack) block.getDrops().toArray()[0];
        Location blockLoc = block.getLocation();
        Location centerOfBlock = blockLoc.add(0.5, 0.5, 0.5);

        // Subtract 5 because 5 items are shot out while spinning
        int numToDrop = Math.max(1, dropTotal - 5);

        final ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -1.35, 0.5), EntityType.ARMOR_STAND);
        armorStand.setMarker(true);
        Objects.requireNonNull(armorStand.getEquipment()).setHelmet(new ItemStack(material));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setFireTicks(1000);

        armorStands.add(armorStand);

        new RepeatingTask(SkillsPlugin.getInstance(), 0, 1) {
            int count = 0;

            @Override
            public void run() {
                if (count <= 22) {
                    block.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation().add(0.5, 1.5, 0.5), 2);
                }

                //Throws remaining blocks out at the end
                if (count >= 60) {
                    Location pLoc = player.getLocation();
                    Vector vector = new Vector(pLoc.getX() - centerOfBlock.getX(), pLoc.getY() - centerOfBlock.getY(), pLoc.getZ() - centerOfBlock.getZ()).normalize().multiply(Math.sqrt(count / 160.0));
                    if (hasSilkTouch) {
                        block.getWorld().dropItem(centerOfBlock, new ItemStack(material, numToDrop)).setVelocity(vector);
                    }
                    else {
                        ItemStack itemStack = nonSilkDrop.clone();
                        itemStack.setAmount(numToDrop);
                        block.getWorld().dropItem(centerOfBlock, itemStack).setVelocity(vector);
                    }

                    armorStands.remove(armorStand);
                    armorStand.remove();
                    cancel();

                    Sounds.spinCycleExplosionSound.playSound(player);
                }

                // Throws 5 blocks out while spinning
                if (count % 10 == 1 && count != 1) {
                    Location pLoc = player.getLocation();
                    Vector vector = new Vector(pLoc.getX() - centerOfBlock.getX(), pLoc.getY() - centerOfBlock.getY(), pLoc.getZ() - centerOfBlock.getZ()).normalize().multiply(Math.sqrt(count / 160.0));
                    if (hasSilkTouch) {
                        block.getWorld().dropItem(centerOfBlock, new ItemStack(material, 1)).setVelocity(vector);
                    }
                    else {
                        block.getWorld().dropItem(centerOfBlock, nonSilkDrop).setVelocity(vector);
                    }
                }

                player.playSound(centerOfBlock, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.1f, (float) (count/75.0 + 0.4));
                armorStand.setHeadPose(armorStand.getHeadPose().add(0, Math.toRadians(count/2.0), 0));
                count++;
            }
        };
    }

    public static ArrayList<ArmorStand> getArmorStands() {
        return armorStands;
    }

}
