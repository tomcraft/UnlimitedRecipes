package fr.tomcraft.unlimitedrecipes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class Updater implements Runnable{

	public static String version = Bukkit.getPluginManager().getPlugin("UnlimitedRecipes").getDescription().getVersion();

	public static boolean updateAvailable = false;

	private static BukkitTask task;

	public static void start()
	{
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("UnlimitedRecipes"), new Updater(), 0L, 864000L);
	}
	
	public static void stop()
	{
		task.cancel();
	}

	public static boolean checkUpdate() 
	{
		try{
			URL url = new URL("https://dl.dropboxusercontent.com/u/66453947/Plugins/UnlimitedRecipes_version.txt");
			URLConnection yc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			if(br.readLine() != version)
			{
				return true;
			}
			return false;
		}catch(Exception e)
		{
			return false;
		}
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
