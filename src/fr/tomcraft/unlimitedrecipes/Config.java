package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

	public Config(Main plugin){
		this.plugin = plugin;
		this.defaultConfig = plugin.getConfig();
		File crafting = new File(plugin.getDataFolder(), "crafting.yml");
		File furnace = new File(plugin.getDataFolder(), "furnace.yml");
		if(!crafting.exists())
		{
			this.extractFile("crafting.yml");

		}
		
		this.crafting = YamlConfiguration.loadConfiguration(crafting);

		if(!furnace.exists())
		{
			this.extractFile("furnace.yml");
		}
		
		this.furnace = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "furnace.yml"));
	}

	public void loadConfigs()
	{
		this.loadCraftingConfig();
		this.loadFurnaceConfig();
	}

	public void loadCraftingConfig()
	{
		loadCraftingRecipes();
	}

	public void loadFurnaceConfig()
	{
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

				if(crafting.getBoolean("config.crafts."+key+".override"))
				{
					plugin.overidenCrafts.put(recipes.getResult().getTypeId()+":"+recipes.getResult().getDurability(), recipes);
				}

				String[] shape = new String[recipe.size()];

				for(int i = 0; i < shape.length; i++)
				{
					shape[i] = recipe.get(i);
				}			

				recipes.shape(shape);

				Set<String> keys2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID").getKeys(false);
				for(String key2 : keys2){
					ConfigurationSection section2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID");
					char c = key2.charAt(0);
					short meta = 0;
					int quantityIng = 1;
					Material material;

					if(section2.getString(key2).contains(":") && section2.getString(key2).contains("x"))
					{
						meta = Short.parseShort(section2.getString(key2).split(":")[1].split("x")[0]);
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
						quantityIng = Integer.parseInt(section2.getString(key2).split("x")[1]);
					}
					else if(section2.getString(key2).contains(":"))
					{
						meta = Short.parseShort(section2.getString(key2).split(":")[1]);
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
					}
					else if(section2.getString(key2).contains("x"))
					{
						meta = Short.parseShort(section2.getString(key2).split("x")[0]);
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
						quantityIng = Integer.parseInt(section2.getString(key2).split("x")[1]);
					}
					else
					{
						material = Material.getMaterial(Integer.parseInt(section2.getString(key2)));
					}

					try{
						recipes.setIngredient(c, new ItemStack(material, quantityIng, meta).getData());
					}catch(Exception e){}
				}
				plugin.customCrafts.add(recipes.getIngredientMap());
				plugin.getServer().addRecipe(recipes);
				System.out.println("[UnlimitedRecipes] Crafting Recipe for: "+toCraft.name()+":"+metadata+" added !");
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

				plugin.getServer().addRecipe(recipe);

				System.out.println("[UnlimitedRecipes] Furnace Recipe for: "+material.name()+" added !");
			}

			System.out.println("[UnlimitedRecipes] All smelt recipes loaded !");
		}
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

	public void extractFile(String file)
	{
		InputStream is = plugin.getResource(file);
		try {
			OutputStream out = new FileOutputStream(new File(plugin.getDataFolder(), file));
			try {
				byte[] buf = new byte[8192];
				int len;

				while ( (len=is.read(buf)) >= 0 ) {
					out.write(buf, 0, len);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
