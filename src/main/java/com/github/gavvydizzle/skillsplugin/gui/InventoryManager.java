package com.github.gavvydizzle.skillsplugin.gui;

import com.github.gavvydizzle.skillsplugin.configs.GUIConfig;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.UUID;

public class InventoryManager implements Listener {

    private final LevelManager levelManager;
    private final ArrayList<UUID> playersInGUI;

    public InventoryManager(LevelManager levelManager) {
        this.levelManager = levelManager;
        playersInGUI = new ArrayList<>();
        reload();
    }

    public void reload() {
        SkillInfoInventory.reload();
        GUIConfig.save();
    }

    /**
     * Opens the SkillInfoInventory for the viewer containing information about a player's skills
     * @param viewer The player to open the inventory for
     * @param stats The player to show the stats of
     * @return If the inventory opened successfully
     */
    public boolean handleSkillInfoInventoryOpen(Player viewer, Player stats) {
        Inventory inventory = levelManager.getSkillInfoInventory(stats);
        if (inventory == null) return false;

        playersInGUI.add(viewer.getUniqueId());
        viewer.openInventory(inventory);
        return true;
    }

    @EventHandler
    private void onMenuClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (playersInGUI.contains(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onMenuClose(InventoryCloseEvent e) {
        playersInGUI.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        playersInGUI.remove(e.getPlayer().getUniqueId());
    }
}
