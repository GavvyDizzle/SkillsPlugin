package com.github.gavvydizzle.skillsplugin.commands.player;

import com.github.gavvydizzle.skillsplugin.commands.PlayerCommandManager;
import com.github.gavvydizzle.skillsplugin.gui.InventoryManager;
import com.github.gavvydizzle.skillsplugin.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OpenSkillInventoryCommand extends SubCommand implements PermissionCommand {

    private final PlayerCommandManager playerCommandManager;
    private final InventoryManager inventoryManager;

    public OpenSkillInventoryCommand(PlayerCommandManager playerCommandManager, InventoryManager inventoryManager) {
        this.playerCommandManager = playerCommandManager;
        this.inventoryManager = inventoryManager;
    }

    @Override
    public String getPermission() {
        return "skillsplugin.skills." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public String getDescription() {
        return "Opens the skill menu";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " menu [player]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length == 1) {
            //TODO - Remove timing
            long old = System.nanoTime();

            if (!inventoryManager.handleSkillInfoInventoryOpen((Player) sender, (Player) sender)) {
                sender.sendMessage(Messages.invalidSelfSkillInventory);
            }

            sender.sendMessage("Open time: " + Numbers.round((System.nanoTime() - old)*1.0/100000L, 3) + "ms");
        }
        else {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
                return;
            }

            if (!inventoryManager.handleSkillInfoInventoryOpen((Player) sender, player)) {
                if (sender == player) {
                    sender.sendMessage(Messages.invalidSelfSkillInventory);
                }
                else {
                    sender.sendMessage(Messages.invalidSkillInventory.replace("{player_name}", player.getName()));
                }
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }
}