package com.github.gavvydizzle.skillsplugin.commands.admin;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.configs.*;
import com.github.gavvydizzle.skillsplugin.utils.Messages;
import com.github.gavvydizzle.skillsplugin.utils.Sounds;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {

    private final ArrayList<String> argsList;

    public ReloadCommand() {
        argsList = new ArrayList<>();
        argsList.add("abilities");
        argsList.add("commands");
        argsList.add("gui");
        argsList.add("levels");
        argsList.add("messages");
        argsList.add("sounds");
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads this plugin or a specified portion";
    }

    @Override
    public String getSyntax() {
        return "/skillAdmin reload [arg]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "abilities":
                    reloadAbilities();
                    SkillsPlugin.getInstance().getInventoryManager().reload(); // Update inventories with new ability info
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded abilities");
                    break;
                case "commands":
                    reloadCommands();
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded commands");
                    break;
                case "gui":
                    reloadGUI();
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded GUI");
                    break;
                case "levels":
                    reloadLevels();
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded levels");
                    break;
                case "messages":
                    reloadMessages();
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded messages");
                    break;
                case "sounds":
                    reloadSounds();
                    sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded sounds");
                    break;
            }
        }
        else {
            reloadAbilities();
            reloadCommands();
            reloadGUI();
            reloadLevels();
            reloadMessages();
            reloadSounds();
            sender.sendMessage(ChatColor.GREEN + "[" + SkillsPlugin.getInstance().getName() + "] " + "Successfully reloaded");
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], argsList, list);
        }

        return list;
    }

    private void reloadAbilities() {
        AbilitiesConfig.reload();
        SkillsPlugin.getInstance().getSkillManager().reload();
    }

    private void reloadCommands() {
        CommandsConfig.reload();
        SkillsPlugin.getInstance().getPlayerCommandManager().reload();
        SkillsPlugin.getInstance().getAdminCommandManager().reload();
    }

    private void reloadGUI() {
        GUIConfig.reload();
        SkillsPlugin.getInstance().getInventoryManager().reload();
    }

    private void reloadLevels() {
        LevelsConfig.reload();
        SkillsPlugin.getInstance().getLevelManager().reload();
    }

    private void reloadMessages() {
        MessagesConfig.reload();
        Messages.reloadMessages();
    }

    private void reloadSounds() {
        SoundsConfig.reload();
        Sounds.reload();
    }
}
