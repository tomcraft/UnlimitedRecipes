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
import java.util.logging.Logger;

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

import fr.tomcraft.unlimitedrecipes.CustomRecipe.RecipeType;

public class Config
{
    private static Logger LOG = Logger.getLogger("Minecraft.UnlimitedRecipes");

    public static FileConfiguration defaultConfig;
    public static FileConfiguration crafting;
    public static FileConfiguration furnace;

    public static void load()
    {
        Config.init();
        Config.loadCraftingRecipes();
        Config.loadFurnaceRecipes();
    }

    private static void init()
    {
        URPlugin plugin = URPlugin.instance;
        Config.defaultConfig = plugin.getConfig();
        if (Config.defaultConfig.get("enableUpdateChecking") == null)
        {
            Config.defaultConfig.set("enableUpdateChecking", true);
            plugin.saveConfig();
        }
        if (Config.defaultConfig.get("enableUpdateDownloading") == null)
        {
            Config.defaultConfig.set("enableUpdateDownloading", false);
            plugin.saveConfig();
        }
        UpdateThread.updateChecking = Config.defaultConfig.getBoolean("enableUpdateChecking");
        UpdateThread.updateDownloading = Config.defaultConfig.getBoolean("enableUpdateDownloading");
        File craftingFile = new File(plugin.getDataFolder(), "crafting.yml");
        File furnaceFile = new File(plugin.getDataFolder(), "furnace.yml");
        if ( !plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdirs();
        }
        if ( !craftingFile.exists())
        {
            Config.extractFile("crafting.yml");
        }
        if ( !furnaceFile.exists())
        {
            Config.extractFile("furnace.yml");
        }
        Config.crafting = YamlConfiguration.loadConfiguration(craftingFile);
        Config.furnace = YamlConfiguration.loadConfiguration(furnaceFile);
    }

    public static void loadCraftingRecipes()
    {
        if (Config.crafting.getConfigurationSection("config.crafts") != null)
        {
            Set<String> keys = Config.crafting.getConfigurationSection("config.crafts").getKeys(false);
            for (String name : keys)
            {
                String key = "config.crafts." + name;
                Material toCraft = Material.getMaterial(Config.crafting.getInt(key + ".itemID"));
                Object metad = Config.crafting.get(key + ".metadata");
                short metadata = 0;
                int quantity = Config.crafting.getInt(key + ".quantity");
                List<String> enchants = Config.crafting.getStringList(key + ".enchantments");
                List<String> lores = Config.crafting.getStringList(key + ".lores");
                RecipeType recipeType = Config.crafting.getBoolean(key + ".shapelessRecipe") ? RecipeType.SHAPELESS_RECIPE : RecipeType.SHAPED_RECIPE;
                boolean deleteOthers = Config.crafting.getBoolean(key + ".deleteOthers");
                String permission = "ur.craft." + name;
                boolean usePermission = Config.crafting.getBoolean(key + ".usePermission");
                String customName = color(Config.crafting.getString(key + ".customName"));
                ItemStack shpedre;
                if (metad instanceof String && (toCraft == Material.SKULL || toCraft == Material.SKULL_ITEM))
                {
                    shpedre = new ItemStack(toCraft, quantity, (short)3);
                    SkullMeta meta = (SkullMeta)shpedre.getItemMeta();
                    meta.setOwner(String.valueOf(metad));
                    shpedre.setItemMeta(meta);
                    metadata = 3;
                }
                else if (metad instanceof String && toCraft.name().contains("LEATHER_"))
                {
                    shpedre = new ItemStack(toCraft, quantity);
                    LeatherArmorMeta meta = (LeatherArmorMeta)shpedre.getItemMeta();
                    meta.setColor(Color.fromRGB(Integer.parseInt(((String)metad).split("r:")[1].split(";")[0].trim()), Integer.parseInt(((String)metad).split("g:")[1].split(";")[0].trim()), Integer.parseInt(((String)metad).split("b:")[1].split(";")[0].trim())));
                    shpedre.setItemMeta(meta);
                }
                else
                {
                    metadata = (short)Config.crafting.getInt(key + ".metadata");
                    shpedre = new ItemStack(toCraft, quantity, metadata);
                }
                if (enchants != null && !enchants.isEmpty())
                {
                    for (String str : enchants)
                    {
                        try
                        {
                            shpedre.addUnsafeEnchantment(Enchantment.getById(Integer.valueOf(str.split(":")[0])), Integer.valueOf(str.split(":")[1]));
                        }
                        catch (Exception e)
                        {
                            LOG.warning("Unable to add enchantment to '" + name + "' " + e.getMessage());
                        }
                    }
                }
                if (customName != null)
                {
                    ItemMeta tmp = shpedre.getItemMeta();
                    tmp.setDisplayName(ChatColor.RESET + customName);
                    shpedre.setItemMeta(tmp);
                }
                if (lores != null && !lores.isEmpty())
                {
                    List<String> lstmp = new ArrayList<String>();
                    for (String s : lores)
                    {
                        lstmp.add(ChatColor.RESET + color(s));
                    }
                    ItemMeta tmp = shpedre.getItemMeta();
                    tmp.setLore(lstmp);
                    shpedre.setItemMeta(tmp);
                }
                Recipe recipes = new ShapedRecipe(shpedre);
                if (recipeType == RecipeType.SHAPELESS_RECIPE)
                {
                    recipes = new ShapelessRecipe(shpedre);
                }
                CustomRecipe custRecipe = new CustomRecipe();
                custRecipe.type = recipeType;
                custRecipe.name = name;
                custRecipe.usePermission = usePermission;
                custRecipe.permission = permission;
                custRecipe.deleteOthers = deleteOthers;
                if (recipeType == RecipeType.SHAPED_RECIPE)
                {
                    List<String> recipe = Config.crafting.getStringList(key + ".recipe");
                    String[] shape = new String[recipe.size()];
                    for (int i = 0; i < shape.length; i++)
                    {
                        shape[i] = recipe.get(i);
                    }
                    ((ShapedRecipe)recipes).shape(shape);
                }
                Set<String> keys2 = Config.crafting.getConfigurationSection(key + ".ingredientsID").getKeys(false);
                for (String key2 : keys2)
                {
                    ConfigurationSection section2 = Config.crafting.getConfigurationSection(key + ".ingredientsID");
                    char c = key2.charAt(0);
                    byte meta = 0;
                    int quantityIng = 1;
                    Material material;
                    if (RecipesManager.getCustomRecipeByName(section2.getString(key2)) != null)
                    {
                        try
                        {
                            if (recipeType == RecipeType.SHAPED_RECIPE)
                            {
                                ((ShapedRecipe)recipes).setIngredient(c, RecipesManager.getCustomRecipeByName(section2.getString(key2)).recipe.getResult().getData());
                            }
                            else
                            {
                                ((ShapelessRecipe)recipes).addIngredient(RecipesManager.getCustomRecipeByName(section2.getString(key2)).recipe.getResult().getData());
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.severe("Error while adding recipe for: " + toCraft.name() + ":" + metadata);
                        }
                    }
                    else
                    {
                        if (section2.getString(key2).contains(":") && section2.getString(key2).contains("x"))
                        {
                            meta = Byte.parseByte(section2.getString(key2).split(":")[1].split("x")[0]);
                            material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
                            quantityIng = Short.parseShort(section2.getString(key2).split("x")[1]);
                        }
                        else if (section2.getString(key2).contains(":"))
                        {
                            meta = Byte.parseByte(section2.getString(key2).split(":")[1]);
                            material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
                        }
                        else if (section2.getString(key2).contains("x"))
                        {
                            meta = Byte.parseByte(section2.getString(key2).split("x")[0]);
                            material = Material.getMaterial(Integer.parseInt(section2.getString(key2).split(":")[0]));
                            quantityIng = Integer.parseInt(section2.getString(key2).split("x")[1]);
                        }
                        else
                        {
                            material = Material.getMaterial(Integer.parseInt(section2.getString(key2)));
                        }
                        try
                        {
                            if (recipeType == RecipeType.SHAPED_RECIPE)
                            {
                                ((ShapedRecipe)recipes).setIngredient(c, material.getNewData(meta));
                            }
                            else
                            {
                                ((ShapelessRecipe)recipes).addIngredient(quantityIng, material.getNewData(meta));
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.severe("Error while adding recipe for: " + toCraft.name() + ":" + metadata);
                        }
                    }
                }
                custRecipe.recipe = recipes;
                RecipesManager.registerRecipe(custRecipe);
                LOG.info("[UnlimitedRecipes] Crafting Recipe for: " + toCraft.name() + ":" + metadata + " added !");
            }
            LOG.info("[UnlimitedRecipes] All craft recipes loaded !");
        }
    }

    public static void loadFurnaceRecipes()
    {
        if (Config.furnace.getConfigurationSection("config.smelts") != null)
        {
            Set<String> keys = Config.furnace.getConfigurationSection("config.smelts").getKeys(false);
            for (String name : keys)
            {
                String key = "config.smelts." + name;
                Material material = Material.getMaterial(Config.furnace.getInt(key + ".resultID"));
                byte metaResult = (byte)Config.furnace.getInt(key + ".result_MetaData");
                String customName = Config.furnace.getString(key + ".result_customName");
                List<String> lores = Config.furnace.getStringList(key + ".result_lores");
                Material ingredient = Material.getMaterial(Config.furnace.getInt(key + ".ingredientID"));
                Config.furnace.getInt(key + ".ingredient_MetaData");
                ItemStack shpedre = new ItemStack(material, 1, metaResult);
                if (customName != null)
                {
                    ItemMeta tmp = shpedre.getItemMeta();
                    tmp.setDisplayName(ChatColor.RESET + color(customName));
                    shpedre.setItemMeta(tmp);
                }
                if (lores != null && !lores.isEmpty())
                {
                    List<String> lstmp = new ArrayList<String>();
                    for (String s : lores)
                    {
                        lstmp.add(ChatColor.RESET + color(s));
                    }
                    ItemMeta tmp = shpedre.getItemMeta();
                    tmp.setLore(lstmp);
                    shpedre.setItemMeta(tmp);
                }
                FurnaceRecipe recipe = new FurnaceRecipe(shpedre, ingredient.getNewData(metaResult));
                RecipesManager.registerRecipe(new CustomRecipe(RecipeType.FURNACE_RECIPE, name, recipe, false, null, false, false));
                LOG.info("[UnlimitedRecipes] Furnace Recipe for: " + material.name() + " added !");
            }
        }
        LOG.info("[UnlimitedRecipes] All smelt recipes loaded !");
    }
    
    public static String color(String string)
    {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }

    public void saveCraftingConfig()
    {
        try
        {
            URPlugin.instance.getDataFolder().mkdirs();
            Config.crafting.save(new File(URPlugin.instance.getDataFolder(), "crafting.yml"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveFurnaceConfig()
    {
        try
        {
            URPlugin.instance.getDataFolder().mkdirs();
            Config.furnace.save(new File(URPlugin.instance.getDataFolder(), "furnace.yml"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void extractFile(String file)
    {
        InputStream is = URPlugin.instance.getResource(file);
        try
        {
            OutputStream out = new FileOutputStream(new File(URPlugin.instance.getDataFolder(), file));
            try
            {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) >= 0)
                {
                    out.write(buf, 0, len);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
