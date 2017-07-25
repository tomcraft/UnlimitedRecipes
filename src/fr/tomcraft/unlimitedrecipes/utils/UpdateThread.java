package fr.tomcraft.unlimitedrecipes.utils;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import fr.tomcraft.unlimitedrecipes.URPlugin;

public class UpdateThread implements Runnable
{

    public static Updater updater;
    public static boolean updateChecking = true;
    public static boolean updateDownloading = false;
    public static boolean updateAvailable = false;
    private static BukkitTask task;
    
    public static void start()
    {
        if (updateChecking && task == null)
        {
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(URPlugin.instance, new UpdateThread(), 0L, 864000L);
        }
    }
    
    public static void stop()
    {
        if (task != null)
        {
            task.cancel();
        }
    }
    
    public static void restart()
    {
        stop();
        start();
    }
    
    public static boolean checkUpdate()
    {
        URPlugin.renewUpdater();
        if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "[UnlimitedRecipes] " + ChatColor.RESET + ChatColor.RED + "An update is available," + (updateDownloading ? " it will be applied on next restart." : " you can get it here: "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/");
            return true;
        }
        return false;
    }
    
    @Override
    public void run()
    {
        updateAvailable = checkUpdate();
    }
}
