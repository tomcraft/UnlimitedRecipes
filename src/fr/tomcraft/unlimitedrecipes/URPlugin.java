package fr.tomcraft.unlimitedrecipes;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class URPlugin extends JavaPlugin
{

    public static URPlugin instance;
    public static Updater updater;

    @Override
    public void onEnable()
    {
        URPlugin.instance = this;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(new RecipesListener(), this);
    }

    public FileConfiguration getCraftingConfig()
    {
        return Config.crafting;
    }

    public FileConfiguration getFurnaceConfig()
    {
        return Config.furnace;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (URPlugin.hasPermission(sender, "ur.reload"))
        {
            if (label.equalsIgnoreCase("ur"))
            {
                if (args.length == 0 || !args[0].equalsIgnoreCase("reload"))
                {
                    sender.sendMessage(ChatColor.RED + "Usage: /ur reload");
                    return false;
                }
                reloadConfig();
            }
        }
        return true;
    }

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