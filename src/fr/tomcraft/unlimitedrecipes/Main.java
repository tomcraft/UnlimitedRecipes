package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;

public class Main extends JavaPlugin{
	
	public Config config;
	
	public void onEnable(){
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
}