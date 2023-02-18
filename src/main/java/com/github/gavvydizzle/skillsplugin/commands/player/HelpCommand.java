package com.github.gavvydizzle.skillsplugin.commands.player;

import com.github.gavvydizzle.skillsplugin.commands.PlayerCommandManager;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends SubCommand implements PermissionCommand {

    private final PlayerCommandManager playerCommandManager;

    public HelpCommand(PlayerCommandManager playerCommandManager) {
        this.playerCommandManager = playerCommandManager;
    }

    @Override
    public String getPermission() {
        return "skillsplugin.skills." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Opens this help menu";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " help";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        playerCommandManager.sendHelpMessage(sender);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }
}