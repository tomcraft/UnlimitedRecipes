package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Config {

	private Main plugin;

	public FileConfiguration defaultConfig;

	public FileConfiguration crafting;

	public FileConfiguration furnace;

	public Config(Main plugin) {
		this.plugin = plugin;
		this.defaultConfig = plugin.getConfig();
		crafting = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "crafting.yml"));
		furnace = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "furnace.yml"));
	}

	public void loadConfigs()
	{
		this.loadCraftingConfig();
		this.loadFurnaceConfig();
	}

	public void loadCraftingConfig()
	{
		if(crafting.get("config") == null)
		{
			setCraftingDefaultConfig();
			this.saveCraftingConfig();
		}

		loadCraftingRecipes();
	}

	public void loadFurnaceConfig()
	{
		if(furnace.get("config") == null)
		{
			setFurnaceDefaultConfig();
			this.saveFurnaceConfig();
		}
			loadFurnaceRecipes();
		
	}

	public void loadCraftingRecipes()
	{
		if(crafting.getConfigurationSection("config.crafts") != null){
			Set<String> keys = crafting.getConfigurationSection("config.crafts").getKeys(false);
			for(String key : keys){

				Material toCraft = Material.getMaterial(crafting.getInt("config.crafts."+key+".itemID"));
				short metadata = (short)crafting.getInt("config.crafts."+key+".metadata");
				int quantity = crafting.getInt("config.crafts."+key+".quantity");
				ArrayList<String> recipe = (ArrayList<String>) crafting.getStringList("config.crafts."+key+".recipe");
				
				ShapedRecipe recipes = new ShapedRecipe(new ItemStack(toCraft, quantity, metadata));
				
				if(crafting.get("config.crafts."+key+".override") == null)
				{
					crafting.set("config.crafts."+key+".override", false);
				}
				
				if(crafting.getBoolean("config.crafts."+key+".override"))
				{
					plugin.overidenCrafts.add(recipes.getResult());
				}
				
				String[] shape = new String[3];
				
				shape[0] = ((recipe.size() >= 1 && recipe.get(0) != null) ? recipe.get(0) : "   ");
				shape[1] = ((recipe.size() >=2 && recipe.get(1) != null) ? recipe.get(1) : "   ");
				shape[2] = ((recipe.size() == 3 && recipe.get(2) != null) ? recipe.get(2) : null);
				
				recipes.shape(shape);

				Set<String> keys2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID").getKeys(false);
				for(String key2 : keys2){
					ConfigurationSection section2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID");
					char c = key2.charAt(0);
					int meta = 0;
					Material material;
					
					if(section2.getString(key2).contains(":"))
					{
						meta = Integer.parseInt(section2.getString(key2).split(":")[1]);
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
					}
					else
					{
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2)));
					}
					try{
						recipes.setIngredient(c, material, meta);
					}catch(Exception e)
					{
						
					}
				}
				plugin.getServer().addRecipe(recipes);
				System.out.println("[UnlimitedRecipes] Crafting Recipe for: "+toCraft.name()+" added !");
			}
			System.out.println("[UnlimitedRecipes] All craft recipes loaded !");
		}
	}


	public void loadFurnaceRecipes()
	{
		if(furnace.getConfigurationSection("config.smelts") != null){
			Set<String> keys = furnace.getConfigurationSection("config.smelts").getKeys(false);
			for(String key : keys){
				ConfigurationSection section = furnace.getConfigurationSection("config.smelts");
				
				Material material = Material.getMaterial(furnace.getInt("config.smelts."+key+".resultID"));
				short metaResult = (short) furnace.getInt("config.smelts."+key+".result_MetaData");

				Material ingredient = Material.getMaterial(furnace.getInt("config.smelts."+key+".ingredientID"));
				short metaIngredient = (short) furnace.getInt("config.smelts."+key+".ingredient_MetaData");
				
				if(furnace.get("config.smelts."+key+".override") == null)
				{
					furnace.set("config.smelts."+key+".override", false);
				}
				
				FurnaceRecipe recipe = new FurnaceRecipe(new ItemStack(material, 1, metaResult), ingredient, metaIngredient);
				
				if(furnace.getBoolean("config.smelts."+key+".override"))
				{
					plugin.overidenSmelts.add(recipe.getResult());
				}				
				
				plugin.getServer().addRecipe(recipe);
				
				System.out.println("[UnlimitedRecipes] Furnace Recipe for: "+material.name()+" added !");
			}
			
			System.out.println("[UnlimitedRecipes] All smelt recipes loaded !");
		}
	}

	public void setCraftingDefaultConfig()
	{
		crafting.options().header("You can get all minecraft IDs on this page: http://www.minecraftinfo.com/IDList.htm");
		
		crafting.set("config.crafts.ice.itemID", 79);
		crafting.set("config.crafts.ice.metadata", 0);
		crafting.set("config.crafts.ice.quantity", 2);
		List<String> list = new ArrayList<String>();
		list.add("aaa");
		list.add("aba");
		list.add("aaa");
		crafting.set("config.crafts.ice.recipe", list);
		crafting.set("config.crafts.ice.override", false);
		crafting.set("config.crafts.ice.ingredientsID.a", "80:0");
		crafting.set("config.crafts.ice.ingredientsID.b", "326:0");
		
		
		crafting.set("config.crafts.saddle.itemID", 329);
		crafting.set("config.crafts.saddle.metadata", 0);
		crafting.set("config.crafts.saddle.quantity", 1);
		List<String> list2 = new ArrayList<String>();
		list2.add("aba");
		list2.add("aca");
		list2.add("aca");
		crafting.set("config.crafts.saddle.recipe", list2);
		crafting.set("config.crafts.saddle.override", false);
		crafting.set("config.crafts.saddle.ingredientsID.a", "334:0");
		crafting.set("config.crafts.saddle.ingredientsID.b", "287:0");
		crafting.set("config.crafts.saddle.ingredientsID.c", "265:0");
	}

	public void setFurnaceDefaultConfig()
	{
		furnace.options().header("You can get all minecraft IDs on this page: http://www.minecraftinfo.com/IDList.htm");
		furnace.set("config.smelts.netherrack.resultID", 87);
		furnace.set("config.smelts.netherrack.result_MetaData", 0);

		furnace.set("config.smelts.netherrack.ingredientID", 1);
		furnace.set("config.smelts.netherrack.ingredient_MetaData", 0);
		furnace.set("config.smelts.netherrack.override", false);
	}

	public void saveCraftingConfig()
	{
		try {
			plugin.getDataFolder().mkdirs();
			crafting.save(new File(plugin.getDataFolder(), "crafting.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveFurnaceConfig()
	{
		try {
			plugin.getDataFolder().mkdirs();
			furnace.save(new File(plugin.getDataFolder(), "furnace.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
