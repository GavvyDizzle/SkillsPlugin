package com.github.gavvydizzle.skillsplugin.commands.admin;

import com.github.gavvydizzle.skillsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.gavvydizzle.skillsplugin.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class SetLevelCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final LevelManager levelManager;

    public SetLevelCommand(AdminCommandManager adminCommandManager, LevelManager levelManager) {
        this.adminCommandManager = adminCommandManager;
        this.levelManager = levelManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "setLevel";
    }

    @Override
    public String getDescription() {
        return "Set a player's skill experience";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " setLevel <player> <type> <level>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return;
        }

        SkillType skillType = SkillType.getFromString(args[2]);
        if (skillType == null) {
            sender.sendMessage(Messages.invalidSkillType.replace("{input}", args[2]));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (Exception ignored) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number");
            return;
        }
        if (level < 0 || level >= levelManager.getMaxLevel()) {
            if (levelManager.getMaxLevel() == Integer.MAX_VALUE) {
                sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number. Provide a number greater than or equal to 0");
            }
            else {
                sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number. Provide a number between 0 and " + levelManager.getMaxLevel());
            }
            return;
        }

        if (levelManager.setPlayerLevel(player, skillType, level)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully set " + player.getName() + "'s " + skillType.name() + " level " + level);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Failed to update " + player.getName() + "'s level. Having them rejoin the server may resolve the issue");
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            return null;
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], SkillType.asStringList, list);
        }

        return list;
    }
}