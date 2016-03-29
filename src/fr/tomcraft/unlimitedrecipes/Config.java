package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import fr.tomcraft.unlimitedrecipes.URecipe.RecipeType;

public class Config
{
    
    static Logger LOG = Logger.getLogger("Minecraft.UnlimitedRecipes");
    public static boolean debug = false;
    public static File craftingFile;
    public static File furnaceFile;
    public static FileConfiguration defaultConfig;
    public static FileConfiguration crafting;
    public static FileConfiguration furnace;
    
    public static void load()
    {
        Config.init();
        Config.loadBlackListedRecipes();
        Config.loadCraftingRecipes();
        Config.loadFurnaceRecipes();
        RecipesManager.registerRecipes();
    }
    
    public static void save()
    {
        URPlugin.instance.saveConfig();
        saveCraftingRecipes();
        saveFurnaceRecipes();
    }
    
    private static void init()
    {
        URPlugin plugin = URPlugin.instance;
        defaultConfig = plugin.getConfig();
        if (!plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdirs();
        }
        if (!new File(plugin.getDataFolder(), "config.yml").exists())
        {
            defaultConfig.set("enableUpdateChecking", true);
            defaultConfig.set("enableUpdateDownloading", false);
            defaultConfig.set("enableBlackList", false);
            defaultConfig.set("blacklisted_items", Arrays.asList(""));
            defaultConfig.set("debug", false);
            plugin.saveConfig();
        }
        debug = defaultConfig.getBoolean("debug");
        craftingFile = new File(plugin.getDataFolder(), "crafting.yml");
        furnaceFile = new File(plugin.getDataFolder(), "furnace.yml");
        crafting = YamlConfiguration.loadConfiguration(craftingFile);
        furnace = YamlConfiguration.loadConfiguration(furnaceFile);
        UpdateThread.updateChecking = defaultConfig.getBoolean("enableUpdateChecking");
        UpdateThread.updateDownloading = defaultConfig.getBoolean("enableUpdateDownloading");
    }
    
    public static void loadBlackListedRecipes()
    {
        if(!defaultConfig.getBoolean("enableBlackList", false))
        {
            return;
        }
        List<String> blackListedItems = defaultConfig.getStringList("blacklisted_items");
        if (blackListedItems != null && !blackListedItems.isEmpty())
        {
            RecipesManager.blacklist.addAll(blackListedItems);
        }
    }
    
    public static void saveBlacklist()
    {
        URPlugin.instance.getConfig().set("blacklisted_items", RecipesManager.blacklist);
        URPlugin.instance.saveConfig();
    }
    
    public static void loadCraftingRecipes()
    {
        ConfigurationSection section = crafting.getConfigurationSection("config.crafts");
        if (section != null)
        {
            Set<String> recipesNames = section.getKeys(false);
            if (recipesNames == null || recipesNames.isEmpty())
            {
                return;
            }
            
            for(String name : recipesNames)
            {
                ConfigurationSection config = section.getConfigurationSection(name);
                RecipeType recipeType = RecipeType.fromName(config.getString("recipeType"));
                boolean enablePermission = config.getBoolean("enablePermission", false);
                boolean disableOthers = config.getBoolean("disableOthers", false);
                boolean transferDurability = config.getBoolean("transferDamage", config.getBoolean("transferDurability", true));
                
                ItemStack result = config.getItemStack("result");
                
                Recipe bukkitRecipe;
                
                if(recipeType == RecipeType.SHAPELESS_RECIPE)
                {
                    ShapelessRecipe recipe = new ShapelessRecipe(result);
                    for(Object o : config.getList("ingredients"))
                    {
                        ItemStack ing = (ItemStack)o;
                        if(ing.getDurability() != -1)
                        {
                            recipe.addIngredient(ing.getAmount(), ing.getData());
                        }
                        else
                        {
                            recipe.addIngredient(ing.getAmount(), ing.getType());
                        }
                    }
                    bukkitRecipe = recipe;
                }
                else if(recipeType == RecipeType.SHAPED_RECIPE)
                {
                    ShapedRecipe recipe = new ShapedRecipe(result);
                    List<String> shape_list = config.getStringList("shape");
                    String[] shape = new String[shape_list.size()];
                    recipe.shape(shape_list.toArray(shape));
                    
                    for(String c : config.getConfigurationSection("ingredients").getKeys(false))
                    {
                        ItemStack ing = config.getItemStack("ingredients."+c);
                        if(ing.getDurability() != -1)
                        {
                            recipe.setIngredient(c.charAt(0), ing.getData());
                        }
                        else
                        {
                            recipe.setIngredient(c.charAt(0), ing.getType());
                        }
                    }
                    
                    bukkitRecipe = recipe;
                }
                else
                {
                    continue;
                }
                
                URecipe uRecipe = new URecipe(bukkitRecipe, recipeType);
                uRecipe.setName(name);
                uRecipe.setEnablePermission(enablePermission);
                uRecipe.setDisableOthers(disableOthers);
                uRecipe.setTransferDamage(transferDurability);
                
                if (Config.debug)
                {
                    Config.LOG.info("[UnlimitedRecipes] Crafting Recipe for: " + result.getType() + ":" + result.getDurability() + " parsed !");
                }
                
                uRecipe.load();
            }
        }
    }
    
    public static void saveCraftingRecipes()
    {
        crafting.set("config.crafts", null);
        ConfigurationSection section = crafting.createSection("config.crafts");
        
        for(URecipe recipe : RecipesManager.customRecipes.values())
        {
            if(recipe.getType() == RecipeType.FURNACE_RECIPE)
            {
                continue;
            }
            
            Recipe bukkitRecipe = recipe.getBukkitRecipe();
            
            ConfigurationSection config = section.createSection(recipe.getName());
            
            config.set("recipeType", recipe.getType().name());
            if(recipe.getType() == RecipeType.SHAPED_RECIPE)
            {
                config.set("shape", Arrays.asList(((ShapedRecipe)bukkitRecipe).getShape()));
            }
            config.set("enablePermission", recipe.enablePermission());
            config.set("disableOthers", recipe.disableOthers());
            config.set("transferDamage", recipe.transferDamage());
            config.set("result", bukkitRecipe.getResult());
            
            if(recipe.getType() == RecipeType.SHAPELESS_RECIPE)
            {
                config.set("ingredients", ((ShapelessRecipe)bukkitRecipe).getIngredientList());
            }
            else if(recipe.getType() == RecipeType.SHAPED_RECIPE)
            {
                config.set("ingredients", ((ShapedRecipe)bukkitRecipe).getIngredientMap());
            }
        }
        
        saveCraftingConfig();
    }
    
    public static void loadFurnaceRecipes()
    {
        ConfigurationSection section = furnace.getConfigurationSection("config.smelts");
        if (section != null)
        {
            Set<String> recipesNames = section.getKeys(false);
            if (recipesNames == null || recipesNames.isEmpty())
            {
                return;
            }
            
            for(String name : recipesNames)
            {
                ConfigurationSection config = section.getConfigurationSection(name);
                boolean disableOthers = config.getBoolean("disableOthers", false);
                ItemStack result = config.getItemStack("result");
                ItemStack ingredient = config.getItemStack("ingredient");
                FurnaceRecipe bukkitRecipe = new FurnaceRecipe(result, ingredient.getData());
                URecipe uRecipe = new URecipe(bukkitRecipe, RecipeType.FURNACE_RECIPE);
                uRecipe.setName(name);
                uRecipe.setDisableOthers(disableOthers);
                uRecipe.setEnablePermission(false);
                uRecipe.setTransferDamage(false);
                
                if (Config.debug)
                {
                    Config.LOG.info("[UnlimitedRecipes] Furnace Recipe for: " + result.getType() + ":" + result.getDurability() + " parsed !");
                }
                
                uRecipe.load();
            }
        }
    }
    
    public static void saveFurnaceRecipes()
    {
        furnace.set("config.smelts", null);
        ConfigurationSection section = furnace.createSection("config.smelts");
        
        for(URecipe recipe : RecipesManager.customRecipes.values())
        {
            if(recipe.getType() != RecipeType.FURNACE_RECIPE)
            {
                continue;
            }
            
            FurnaceRecipe bukkitRecipe = (FurnaceRecipe)recipe.getBukkitRecipe();
            
            ConfigurationSection config = section.createSection(recipe.getName());
            
            config.set("disableOthers", recipe.disableOthers());
            config.set("result", bukkitRecipe.getResult());
            config.set("ingredient", bukkitRecipe.getInput());
        }
        
        saveFurnaceConfig();
    }
    
    public static String color(String string)
    {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }
    
    public static void saveCraftingConfig()
    {
        try
        {
            URPlugin.instance.getDataFolder().mkdirs();
            Config.crafting.save(Config.craftingFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void saveFurnaceConfig()
    {
        try
        {
            URPlugin.instance.getDataFolder().mkdirs();
            Config.furnace.save(Config.furnaceFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
}
