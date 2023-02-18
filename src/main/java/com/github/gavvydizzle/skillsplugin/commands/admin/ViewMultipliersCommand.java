package com.github.gavvydizzle.skillsplugin.commands.admin;

import com.github.gavvydizzle.skillsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ViewMultipliersCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final LevelManager levelManager;

    public ViewMultipliersCommand(AdminCommandManager adminCommandManager, LevelManager levelManager) {
        this.adminCommandManager = adminCommandManager;
        this.levelManager = levelManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "viewMultipliers";
    }

    @Override
    public String getDescription() {
        return "View all multipliers";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " viewMultipliers";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        levelManager.printMultipliers(sender);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }
}