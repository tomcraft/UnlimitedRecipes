package fr.tomcraft.unlimitedrecipes;

import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

public class UpdateThread implements Runnable
{

    public static boolean updateChecking = true;
    public static boolean updateDownloading = false;
    public static boolean updateAvailable = false;
    private static BukkitTask task;

    public static void start()
    {
        if (UpdateThread.updateChecking)
        {
            UpdateThread.task = Bukkit.getScheduler().runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("UnlimitedRecipes"), new UpdateThread(), 0L, 864000L);
        }
    }

    public static void stop()
    {
        UpdateThread.task.cancel();
    }

    public static boolean checkUpdate()
    {
        URPlugin.renewUpdater();
        if (URPlugin.updater.getResult() == UpdateResult.UPDATE_AVAILABLE)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "[UnlimitedRecipes] " + ChatColor.RESET + ChatColor.RED + "An update is available," + (UpdateThread.updateDownloading ? " it will be applied on next restart." : " you can get it here: "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/");
            return true;
        }
        return false;
    }

    @Override
    public void run()
    {
        UpdateThread.updateAvailable = UpdateThread.checkUpdate();
    }
    
}
