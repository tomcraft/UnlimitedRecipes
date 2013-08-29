package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Config {

	private Main plugin;

	public FileConfiguration defaultConfig;

	public FileConfiguration crafting;

	public FileConfiguration furnace;

	public Config(Main plugin){
		this.plugin = plugin;
		this.defaultConfig = plugin.getConfig();

		if(defaultConfig.get("enableUpdateChecking") == null)
		{
			defaultConfig.set("enableUpdateChecking", true);
			plugin.saveConfig();
		}

		Updater.updateChecking = defaultConfig.getBoolean("enableUpdateChecking");

		File crafting = new File(plugin.getDataFolder(), "crafting.yml");
		File furnace = new File(plugin.getDataFolder(), "furnace.yml");

		if(!plugin.getDataFolder().exists())
		{
			plugin.getDataFolder().mkdirs();
		}

		if(!crafting.exists())
		{
			this.extractFile("crafting.yml");
		}

		this.crafting = YamlConfiguration.loadConfiguration(crafting);

		if(!furnace.exists())
		{
			this.extractFile("furnace.yml");
		}

		this.furnace = YamlConfiguration.loadConfiguration(furnace);
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
				Object metad = crafting.get("config.crafts."+key+".metadata");
				short metadata = 0;
				int quantity = crafting.getInt("config.crafts."+key+".quantity");

				List<String> enchants = crafting.getStringList("config.crafts."+key+".enchantments");

				List<String> lores = crafting.getStringList("config.crafts."+key+".lores");

				boolean shapelessRecipe = crafting.getBoolean("config.crafts."+key+".shapelessRecipe");

				boolean override = crafting.getBoolean("config.crafts."+key+".override");

				String permission = "ur.craft." + key;

				boolean usePermission = crafting.getBoolean("config.crafts."+key+".usePermission");

				String customName = crafting.getString("config.crafts."+key+".customName");

				ItemStack shpedre;

				if(metad instanceof String && (toCraft == Material.SKULL || toCraft == Material.SKULL_ITEM))
				{
					shpedre = new ItemStack(toCraft, quantity, (short) 3);
					SkullMeta meta = (SkullMeta)shpedre.getItemMeta();
					meta.setOwner(String.valueOf(metad));
					shpedre.setItemMeta(meta);
					metadata = 3;
				}
				else if(metad instanceof String && (toCraft.name().contains("LEATHER_")))
				{
					shpedre = new ItemStack(toCraft, quantity);
					LeatherArmorMeta meta = (LeatherArmorMeta) shpedre.getItemMeta();
					meta.setColor(Color.fromRGB(Integer.parseInt(((String)metad).split("r:")[1].split(";")[0].trim()), Integer.parseInt(((String)metad).split("g:")[1].split(";")[0].trim()), Integer.parseInt(((String)metad).split("b:")[1].split(";")[0].trim())));
					shpedre.setItemMeta(meta);
				}
				else
				{
					metadata = (short)crafting.getInt("config.crafts."+key+".metadata");
					shpedre = new ItemStack(toCraft, quantity, metadata);
				}

				if(enchants != null && !enchants.isEmpty()){
					for(String str : enchants)
					{
						try{
							shpedre.addEnchantment(Enchantment.getById(Integer.valueOf(str.split(":")[0])) , Integer.valueOf(str.split(":")[1]));
						}catch(Exception e){}
					}
				}

				if(customName != null)
				{
					ItemMeta tmp = shpedre.getItemMeta();
					tmp.setDisplayName(ChatColor.RESET + customName.replaceAll("(&([a-f0-9]))", "§$2"));
					shpedre.setItemMeta(tmp);
				}

				if(lores != null && !lores.isEmpty()){
					List<String> lstmp = new ArrayList<String>();
					for(String s : lores)
					{
						lstmp.add(ChatColor.RESET + s.replaceAll("(&([a-f0-9]))", "§$2"));
					}
					ItemMeta tmp = shpedre.getItemMeta();
					tmp.setLore(lstmp);
					shpedre.setItemMeta(tmp);
				}

				Recipe recipes = new ShapedRecipe(shpedre);

				CustomRecipe custRecipe = new CustomShapedRecipe();

				if(shapelessRecipe)
				{
					recipes = new ShapelessRecipe(shpedre);
					custRecipe = new CustomShapelessRecipe();
				}

				custRecipe.name = key;
				custRecipe.plugin = plugin;
				custRecipe.usePermission = usePermission;
				custRecipe.permission = permission;
				custRecipe.override = override;


				if(!shapelessRecipe)
				{
					ArrayList<String> recipe = (ArrayList<String>) crafting.getStringList("config.crafts."+key+".recipe");
					String[] shape = new String[recipe.size()];

					for(int i = 0; i < shape.length; i++)
					{
						shape[i] = recipe.get(i);
					}

					((ShapedRecipe)recipes).shape(shape);
				}

				Set<String> keys2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID").getKeys(false);
				for(String key2 : keys2){
					ConfigurationSection section2 = crafting.getConfigurationSection("config.crafts."+key+".ingredientsID");
					char c = key2.charAt(0);
					short meta = 0;
					int quantityIng = 1;
					Material material;

					if(plugin.getCustomRecipeByName(section2.getString(key2)) != null)
					{
						try{
							if(!shapelessRecipe)
							{
								((ShapedRecipe)recipes).setIngredient(c, plugin.getCustomRecipeByName(section2.getString(key2)).recipe.getResult().getData());
							}
							else
							{
								((ShapelessRecipe)recipes).addIngredient(plugin.getCustomRecipeByName(section2.getString(key2)).recipe.getResult().getData());
							}

						}catch(Exception e){
							System.out.println("ERROR DURRING ADDING RECIPE FOR: "+toCraft.name()+":"+metadata);
						}
					}else{

						if(section2.getString(key2).contains(":") && section2.getString(key2).contains("x"))
						{
							meta = Short.parseShort(section2.getString(key2).split(":")[1].split("x")[0]);
							material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
							quantityIng = Short.parseShort(section2.getString(key2).split("x")[1]);
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
							if(!shapelessRecipe)
							{
								((ShapedRecipe)recipes).setIngredient(c, material, meta);
							}
							else
							{
								((ShapelessRecipe)recipes).addIngredient(quantityIng, material, meta);
							}

						}catch(Exception e){
							System.out.println("ERREUR DURRING ADDING RECIPE FOR: "+toCraft.name()+":"+metadata);
						}
					}
				}

				custRecipe.recipe = recipes;
				if(custRecipe instanceof CustomShapedRecipe){
					((CustomShapedRecipe)custRecipe).ingredients = ((ShapedRecipe)recipes).getIngredientMap();
					plugin.customShapedCrafts.add(((CustomShapedRecipe)custRecipe).ingredients);
				}
				custRecipe.register();
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
				//ConfigurationSection section = furnace.getConfigurationSection("config.smelts");

				Material material = Material.getMaterial(furnace.getInt("config.smelts."+key+".resultID"));
				short metaResult = (short) furnace.getInt("config.smelts."+key+".result_MetaData");

				String customName = furnace.getString("config.smelts."+key+".result_customName");
				List<String> lores = furnace.getStringList("config.smelts."+key+".result_lores");

				Material ingredient = Material.getMaterial(furnace.getInt("config.smelts."+key+".ingredientID"));
				short metaIngredient = (short) furnace.getInt("config.smelts."+key+".ingredient_MetaData");

				ItemStack shpedre = new ItemStack(material, 1, metaResult);

				if(customName != null)
				{
					ItemMeta tmp = shpedre.getItemMeta();
					tmp.setDisplayName(ChatColor.RESET + customName.replaceAll("(&([a-f0-9]))", "§$2"));
					shpedre.setItemMeta(tmp);
				}

				if(lores != null && !lores.isEmpty()){
					List<String> lstmp = new ArrayList<String>();
					for(String s : lores)
					{
						lstmp.add(ChatColor.RESET + s.replaceAll("(&([a-f0-9]))", "§$2"));
					}
					ItemMeta tmp = shpedre.getItemMeta();
					tmp.setLore(lstmp);
					shpedre.setItemMeta(tmp);
				}

				FurnaceRecipe recipe = new FurnaceRecipe(shpedre, ingredient, metaIngredient);

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
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
