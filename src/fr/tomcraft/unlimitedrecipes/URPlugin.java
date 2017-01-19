package fr.tomcraft.unlimitedrecipes;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.tomcraft.unlimitedrecipes.commands.Blacklist;
import fr.tomcraft.unlimitedrecipes.commands.Create;
import fr.tomcraft.unlimitedrecipes.commands.Help;
import fr.tomcraft.unlimitedrecipes.commands.ItemConfig;
import fr.tomcraft.unlimitedrecipes.commands.Tools;
import fr.tomcraft.unlimitedrecipes.utils.CommandController;
import fr.tomcraft.unlimitedrecipes.utils.UpdateThread;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class URPlugin extends JavaPlugin
{
    
    public static URPlugin instance;
    public static Updater updater;

    public static HashMap<String, URecipe> craftMaking = new HashMap<String, URecipe>();
    public static HashMap<String, ItemStack> craftMakingResultTMP = new HashMap<String, ItemStack>();
    
    public static HashMap<String, URecipe> craftViewers = new HashMap<String, URecipe>();
    
    @Override
    public void onEnable()
    {
        URPlugin.instance = this;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(new RecipesListener(), this);
        CommandController.registerCommands(instance, new Blacklist());
        CommandController.registerCommands(instance, new Create());
        CommandController.registerCommands(instance, new Help());
        CommandController.registerCommands(instance, new ItemConfig());
        CommandController.registerCommands(instance, new Tools());
    }
    
    public FileConfiguration getCraftingConfig()
    {
        return Config.crafting;
    }
    
    public FileConfiguration getFurnaceConfig()
    {
        return Config.furnace;
    }
    
    public static String[] subArgs(String[] args)
    {
        String[] subArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++)
        {
            subArgs[i - 1] = args[i];
        }
        return subArgs;
    }
    
    @Override
    public void reloadConfig()
    {
        super.reloadConfig();
        RecipesManager.reset();
        Config.load();
        UpdateThread.restart();
    }
    
    public static boolean hasPermission(CommandSender sender, String perm)
    {
        return sender.hasPermission(perm) || sender.isOp();
    }
    
    public static void renewUpdater()
    {
        URPlugin.updater = new Updater(URPlugin.instance, 52907, URPlugin.instance.getFile(), UpdateThread.updateDownloading ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD, false);
    }
}