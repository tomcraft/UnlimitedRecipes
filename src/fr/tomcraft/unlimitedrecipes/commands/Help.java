package fr.tomcraft.unlimitedrecipes.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import fr.tomcraft.unlimitedrecipes.utils.CommandController.CommandHandler;
import fr.tomcraft.unlimitedrecipes.utils.CommandController.SubCommandHandler;

public class Help
{
    
    public static final int maxLines = 10;
    
    private static List<String> helps = Arrays.asList(
            "Some commands needs you to have the item in your hand",
            "In a text put %player% to include the player name",
            "In a text put _ to make a space"
            );
    
    private static List<String> usages = Arrays.asList(
            "/ur help <page>",
            "/ur create <name> <shaped/shapeless> [disableOthers] [enablePermission] [transfertDurability]",
            "/ur create <name> furnace [disableOthers]",
            "/ur list",
            "/ur view <name>",
            "/ur delete <name>",
            "/ur blacklist <on/off>",
            "/ur blacklist add [useData] (true or false)",
            "/ur blacklist delete",
            "/ur blacklist list",
            "/ur item rename <name>",
            "/ur item lore add <line>",
            "/ur item lore reset",
            "/ur item enchant add <enchant> <level>",
            "/ur item enchant list",
            "/ur item enchant reset",
            "/ur item unbreakable <true/false>",
            "/ur item skull <ownerName or %player%>",
            "/ur item hide attributes <true/false>",
            "/ur item hide destroys <true/false>",
            "/ur item hide enchants <true/false>",
            "/ur item hide potion_effects <true/false>",
            "/ur item hide placed_on <true/false>",
            "/ur item hide unbreakable <true/false>",
            "/ur reload"
            );
    
    public static List<String> getUsages(String command)
    {
        ArrayList<String> result = new ArrayList<String>();
        for(String line : usages)
        {
            if(command == null || line.startsWith(command))
            {
                result.add(line);
            }
        }
        return result;
    }
    
    public static void showUsages(CommandSender sender, String command)
    {
        List<String> usages = getUsages(null);
        for(String usage : usages)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + usage);
        }
    }
    
    public static void showHelp(CommandSender sender, int page)
    {
        List<String> usages = getUsages(null);
        int maxPage = usages.size() / maxLines + (usages.size() % maxLines != 0 ? 1 : 0);
        
        if(page < 0)
        {
            page = 0;
        }
        else if(page > maxPage)
        {
            page = maxPage;
        }
        
        sender.sendMessage(ChatColor.GOLD + "---------- UnlimitedRecipes Help page " + page+1 + "/" + maxPage + " ----------");
        for(String help : helps)
        {
            sender.sendMessage(ChatColor.GRAY + help);
        }
        for(int i = maxLines * page; i < maxLines * page + maxLines; i++)
        {
            if(i < usages.size())
            {
                sender.sendMessage(ChatColor.RED + "Usage: " + usages.get(i));
            }
        }
    }
    
    @CommandHandler(name = "ur", description = "The UnlimitedRecipes main command", usage = "/ur help")
    public void mainUR(CommandSender sender, String args[])
    {
        if(args.length > 0)
        {
            sender.sendMessage(ChatColor.RED + "Command syntax error, use /ur help");
            return;
        }
        showHelp(sender, 0);
    }
    
    @SubCommandHandler(name = "help", parent = "ur")
    public void help(CommandSender sender, String args[])
    {
        int page = args.length >= 1 ? Integer.parseInt(args[0]) : 0;
        showHelp(sender, page);
    }
}
