package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	public Config config;
	public ArrayList<ItemStack> overidenCrafts;
	public ArrayList<ItemStack> overidenSmelts;
	
	public void onEnable(){
		overidenCrafts = new ArrayList<ItemStack>();
		overidenSmelts = new ArrayList<ItemStack>();
		config = new Config(this);
		config.loadConfigs();
	}
	
	public FileConfiguration getCraftingConfig()
	{
		return config.crafting;
	}
	
	public FileConfiguration getFurnaceConfig()
	{
		return config.furnace;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("ur"))
		{
			if(args.length == 0 || !args[0].equalsIgnoreCase("reload"))
			{
				sender.sendMessage(ChatColor.RED+"Usage: /ur reload");
				return false;
			}
			
			this.getServer().resetRecipes();
			
			this.config = null;
			
			this.onEnable();
			
		}
		return true;
	}
}