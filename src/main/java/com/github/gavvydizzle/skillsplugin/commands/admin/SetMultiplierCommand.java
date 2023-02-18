package com.github.gavvydizzle.skillsplugin.commands.admin;

import com.github.gavvydizzle.skillsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.skillsplugin.player.LevelManager;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.gavvydizzle.skillsplugin.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Set the global, all skills, or a specific skill's multiplier
 */
public class SetMultiplierCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final LevelManager levelManager;
    private final ArrayList<String> args1;

    public SetMultiplierCommand(AdminCommandManager adminCommandManager, LevelManager levelManager) {
        this.adminCommandManager = adminCommandManager;
        this.levelManager = levelManager;

        args1 = new ArrayList<>(SkillType.asStringList);
        args1.add("all");
        args1.add("global");
        Collections.sort(args1);
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "setMultiplier";
    }

    @Override
    public String getDescription() {
        return "Set the global or a skill-specific XP multiplier";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " setMultiplier <all|global|skill> <multiplier>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[2]);
        } catch (Exception ignored) {
            sender.sendMessage(ChatColor.RED + args[2] + " is not a valid multiplier. Provide a number");
            return;
        }
        if (multiplier < 0) multiplier = 0.0;

        String multType = args[1].toLowerCase();
        if (!args1.contains(multType)) {
            sender.sendMessage(ChatColor.RED + "Invalid multiplier type");
            return;
        }

        if (multType.equals("global")) {
            double newMult = levelManager.setGlobalMultiplier(multiplier);
            if (newMult == -1) {
                sender.sendMessage(ChatColor.RED + "Failed to update the global multiplier");
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "Successfully updated the global multiplier to " + newMult + "x");
            }
        }
        else if (multType.equals("all")) {
            if (levelManager.setAllSkillMultipliers(multiplier)) {
                sender.sendMessage(ChatColor.GREEN + "Successfully updates all skill multipliers to " + multiplier + "x");
            }
            else {
                sender.sendMessage(ChatColor.RED + "Failed to update skill multipliers. It is very unlikely, but some multipliers could have been updated");
            }
        }
        else {
            SkillType skillType = SkillType.getFromString(args[1]);
            if (skillType == null) {
                sender.sendMessage(Messages.invalidSkillType.replace("{input}", args[1]));
                return;
            }

            double newMult = levelManager.setSkillMultiplier(skillType, multiplier);
            if (newMult == -1) {
                sender.sendMessage(ChatColor.RED + "Failed to update the " + skillType.name() + " multiplier");
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "Successfully updated the " + skillType.name() + " multiplier to " + newMult + "x");
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], args1, list);
        }

        return list;
    }
}