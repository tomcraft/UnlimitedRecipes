package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.Map;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public static String PACKAGE_NAME_SERVER;
	public static String PACKAGE_NAME_CRAFTBUKKIT;
	public Config config;
	public ArrayList<CustomRecipe> customRecipes;
	public ArrayList<Map<Character,ItemStack>> customShapedCrafts;
	private Permission permission;

	public void onEnable(){
		try{
			Package[] a = Package.getPackages();
			Package pl = null;
			for(Package p : a)
			{
				if(p.getName().startsWith("net.minecraft.server"))
				{
					try{
						Class.forName(p.getName()+".Block");
						pl = p;
						Main.PACKAGE_NAME_SERVER = p.getName();
					}catch(Exception e){}
				}
				else if(p.getName().startsWith("org.bukkit.craftbukkit"))
				{
					try{
						Class.forName(p.getName()+".CraftServer");
						pl = p;
						Main.PACKAGE_NAME_CRAFTBUKKIT = p.getName();
					}catch(Exception e){}
				}
					
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		customRecipes = new ArrayList<CustomRecipe>();
		customShapedCrafts = new ArrayList<Map<Character,ItemStack>>();
		config = new Config(this);
		config.loadConfigs();
		Updater.start();
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
	
	public CustomRecipe getCustomRecipeByRecipe(Recipe recipe)
	{
		for(CustomRecipe cust : this.customRecipes)
		{
			if(recipe instanceof ShapedRecipe && cust.recipe instanceof ShapedRecipe)
			{
				if(this.customShapedCrafts.contains(((ShapedRecipe)cust.recipe).getIngredientMap()) && recipe.getResult().getType() == cust.recipe.getResult().getType() && recipe.getResult().getDurability() == cust.recipe.getResult().getDurability())
				{
					return (CustomShapedRecipe)cust;
				}
			}
			else if(recipe instanceof ShapelessRecipe && cust.recipe instanceof ShapelessRecipe)
			{
				if(((ShapelessRecipe)cust.recipe).getIngredientList().equals(((ShapelessRecipe)recipe).getIngredientList()))
				{
					return (CustomShapelessRecipe)cust;
				}
			}
			else if(recipe instanceof FurnaceRecipe && cust.recipe instanceof FurnaceRecipe)
			{
				if(((FurnaceRecipe)cust.recipe).getInput().equals(((FurnaceRecipe)recipe).getInput()))
				{
					return (CustomFurnaceRecipe)cust;
				}
			}
			
		}
		return null;
	}
	
	public CustomRecipe getCustomRecipeByResult(String result)
	{
		for(CustomRecipe cust : this.customRecipes)
		{
			if((cust.recipe.getResult().getTypeId()+":"+cust.recipe.getResult().getDurability()).equals(result))
			{
				return cust;
			}
		}
		return null;
	}
	
	public CustomRecipe getCustomRecipeByName(String name)
	{
		for(CustomRecipe cust : this.customRecipes)
		{
			if(cust.name.equalsIgnoreCase(name))
			{
				return cust;
			}
		}
		return null;
	}
	
	public boolean isCustomRecipe(Recipe recipe)
	{
		if(getCustomRecipeByRecipe(recipe) != null)
		{
			return true;
		}
		return false;
	}
	
}