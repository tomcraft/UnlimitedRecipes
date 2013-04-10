package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public Config config;
	public ArrayList<Map<Character,ItemStack>> customCrafts;
	public HashMap<String, ShapedRecipe> overidenCrafts;
	private Permission permission;

	public void onEnable(){
		
		this.getResource("crafting.yml");
		
		customCrafts = new ArrayList<Map<Character,ItemStack>>();
		overidenCrafts = new HashMap<String, ShapedRecipe>();
		config = new Config(this);
		config.loadConfigs();
		setupPermissions();
		this.getServer().getPluginManager().registerEvents(new RecipesListener(this), this);
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
		if(hasPermission(sender, "ur.reload")){

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
		}
		return true;
	}

	public boolean hasPermission(CommandSender player, String permission)
	{
		return ((this.permission != null && this.permission.has(player, permission)) || player.isOp());
	}

	public boolean hasPermission(String player, String permission)
	{
		return ((this.permission != null && this.permission.has(getServer().getPlayer(player), permission)) || getServer().getPlayer(player).isOp());
	}

	private boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
}