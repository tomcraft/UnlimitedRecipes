package fr.tomcraft.unlimitedrecipes;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateType;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class URPlugin extends JavaPlugin
{

    public static URPlugin instance;
    private static Permission permission;
    public static Updater updater;

    @Override
    public void onEnable()
    {
        URPlugin.instance = this;
        Config.load();
        UpdateThread.start();
        setupPermissions();
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
                Bukkit.resetRecipes();
                RecipesManager.reset();
                onEnable();
            }
        }
        return true;
    }

    public static boolean hasPermission(CommandSender player, String perm)
    {
        return (URPlugin.permission != null && URPlugin.permission.has(player, perm)) || player.isOp();
    }

    public static boolean hasPermission(String player, String perm)
    {
        return hasPermission(Bukkit.getPlayer(player), perm);
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
        {
            URPlugin.permission = permissionProvider.getProvider();
        }
        return URPlugin.permission != null;
    }

    public static void renewUpdater()
    {
        URPlugin.updater = new Updater(URPlugin.instance, 52907, URPlugin.instance.getFile(), UpdateThread.updateDownloading ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD, false);
    }
}