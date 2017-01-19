package fr.tomcraft.unlimitedrecipes.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.tomcraft.unlimitedrecipes.Config;
import fr.tomcraft.unlimitedrecipes.RecipesManager;
import fr.tomcraft.unlimitedrecipes.URPlugin;
import fr.tomcraft.unlimitedrecipes.utils.CommandController.SubCommandHandler;

public class Blacklist
{
    private URPlugin plugin = URPlugin.instance;
    private static List<String> subCommands = Arrays.asList("on", "off", "add", "delete", "list");
    
    @SubCommandHandler(name = "blacklist", parent = "ur", permission = "ur.blacklist")
    public void blacklist(Player player, String args[])
    {
        if(args.length == 0 || !subCommands.contains(args[0].toLowerCase()))
        {
            Help.showUsages(player, "/ur blacklist");
            return;
        }
        
        String action = args[0];
        boolean useData = args.length >= 2 ? Boolean.parseBoolean(args[1]) : true;
        ItemStack item = player.getItemInHand();
        ArrayList<String> blackList = RecipesManager.blacklist;
        
        if(action.equalsIgnoreCase("on"))
        {
            plugin.getConfig().set("enableBlackList", true);
            player.sendMessage(ChatColor.GREEN + "Done");
        }
        else if(action.equalsIgnoreCase("off"))
        {
            plugin.getConfig().set("enableBlackList", false);
            player.sendMessage(ChatColor.GREEN + "Done");
        }
        else if(action.equalsIgnoreCase("add"))
        {
            blackList.add(item.getType() + (useData ? ":" + item.getData().getData() : ""));
            player.sendMessage(ChatColor.GREEN + "Recipe blacklisted !");
        }
        else if(action.equalsIgnoreCase("remove"))
        {
            for(String s : new ArrayList<String>(blackList))
            {
                Material mat = Material.matchMaterial(s.split(":")[0]);
                byte data = s.contains(":") ? Byte.parseByte(s.split(":")[1]) : -1;
                
                if(mat != item.getType() || s.contains(":") && data != item.getData().getData())
                {
                    continue;
                }
                
                blackList.remove(s);
            }
            player.sendMessage(ChatColor.GREEN + "Recipe unblacklisted !");
        }
        else if(action.equalsIgnoreCase("list"))
        {
            for(String s : blackList)
            {
                player.sendMessage(ChatColor.GREEN + " - " + s);
            }
            player.sendMessage(ChatColor.GREEN + "Done");
        }
        
        RecipesManager.blacklist = blackList;
        Config.saveBlacklist();
        
        RecipesManager.reload();
    }
    
}
