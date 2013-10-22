package fr.tomcraft.unlimitedrecipes;

import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

public class UpdateThread implements Runnable{

	public static boolean updateChecking = true;
	
	public static boolean updateDownloading = false;
	
	public static boolean updateAvailable = false;

	private static BukkitTask task;
	

	public static void start()
	{
		if(updateChecking)
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("UnlimitedRecipes"), new UpdateThread(), 0L, 864000L);
	}
	
	public static void stop()
	{
		task.cancel();
	}

	public static boolean checkUpdate() 
	{
		Main.renewUpdater();
		if(Main.updater.getResult() == UpdateResult.UPDATE_AVAILABLE)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "[UnlimitedRecipes] " + ChatColor.RESET + ChatColor.RED + "An update is available,"+ (UpdateThread.updateDownloading ? " it will be apply on next restart." : " you can get it here: "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/");
            return true;
		}
		return false;
	}

	@Override
	public void run() {
		new Thread()
		{
			public void run()
			{
				updateAvailable = checkUpdate();
			}
		}.start();
	}

}
