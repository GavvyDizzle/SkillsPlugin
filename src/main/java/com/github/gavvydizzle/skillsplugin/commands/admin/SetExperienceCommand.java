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

public class SetExperienceCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final LevelManager levelManager;

    public SetExperienceCommand(AdminCommandManager adminCommandManager, LevelManager levelManager) {
        this.adminCommandManager = adminCommandManager;
        this.levelManager = levelManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "setXP";
    }

    @Override
    public String getDescription() {
        return "Set a player's skill experience";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " setXP <player> <type> <percent>";
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

        double percent;
        try {
            percent = Double.parseDouble(args[3]);
        } catch (Exception ignored) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number. Provide a number between 0 and 1 [0,1)");
            return;
        }
        if (percent < 0 || percent >= 1) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number. Provide a number between 0 and 1 [0,1)");
            return;
        }

        if (levelManager.setPlayerSkillXP(player, skillType, percent)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully set " + player.getName() + "'s " + skillType.name() + " to " + percent + " of the way to the next level");
        }
        else {
            sender.sendMessage(ChatColor.RED + "Failed to update " + player.getName() + "'s xp. Having them rejoin the server may resolve the issue");
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