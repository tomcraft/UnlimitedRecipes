package fr.tomcraft.unlimitedrecipes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    }
    
    private static void init()
    {
        URPlugin plugin = URPlugin.instance;
        Config.defaultConfig = plugin.getConfig();
        
        if ( !plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdirs();
        }
        
        if (!new File(plugin.getDataFolder(), "config.yml").exists())
        {
            Config.defaultConfig.set("enableUpdateChecking", true);
            Config.defaultConfig.set("enableUpdateDownloading", false);
            Config.defaultConfig.set("blacklisted_items", Arrays.asList("STONE:0", "WORKBENCH", "61"));
            Config.defaultConfig.set("debug", false);
            plugin.saveConfig();
        }
        
        debug = Config.defaultConfig.getBoolean("debug");
        
        UpdateThread.updateChecking = Config.defaultConfig.getBoolean("enableUpdateChecking");
        UpdateThread.updateDownloading = Config.defaultConfig.getBoolean("enableUpdateDownloading");
        
        craftingFile = new File(plugin.getDataFolder(), "crafting.yml");
        furnaceFile = new File(plugin.getDataFolder(), "furnace.yml");
        
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
    
    private static boolean isInt(String obj)
    {
        try
        {
            Integer.parseInt(obj);
            return true;
        }catch(Exception e)
        {
            return false;
        }
    }
    
    private static Material getMaterial(String obj)
    {
        if(isInt(obj))
        {
            return Material.getMaterial(Integer.parseInt(obj));
        }
        else
        {
            return Material.getMaterial(obj);
        }
    }
    
    public static void loadBlackListedRecipes()
    {
        if (Config.defaultConfig.getStringList("blacklisted_items") != null)
        {
            List<String> blackListedItems = Config.defaultConfig.getStringList("blacklisted_items");
            
            if(blackListedItems.isEmpty())
            {
                return;
            }
            
            for(String item: blackListedItems)
            {
                Material mat = null;
                byte data = -1;
                
                if(item.contains(":"))
                {
                    mat = getMaterial(item.split(":")[0]);
                    data = Byte.parseByte(item.split(":")[1]);
                    RecipesManager.unloadBukkitRecipes(mat, data);
                    if(debug)
                    {
                        LOG.info("[UnlimitedRecipes] All recipes for " + mat.name() + ":" + data + " were deleted !");
                    }
                }
                else
                {
                    mat = getMaterial(item);
                    RecipesManager.unloadBukkitRecipes(mat);
                    if(debug)
                    {
                        LOG.info("[UnlimitedRecipes] All recipes for " + mat.name() + " were deleted !");
                    }
                }
            }
            
            LOG.info("[UnlimitedRecipes] Recipes were deleted ! (" + blackListedItems.size() + " items)");
        }
    }
    
    public static void loadCraftingRecipes()
    {
        if (Config.crafting.getConfigurationSection("config.crafts") != null)
        {
            Set<String> keys = Config.crafting.getConfigurationSection("config.crafts").getKeys(false);
            
            if(keys == null || keys.isEmpty())
            {
                return;
            }
            
            for (String name : keys)
            {
                String key = "config.crafts." + name;
                Material toCraft = getMaterial(Config.crafting.getString(key + ".itemID"));
                Object metad = Config.crafting.get(key + ".metadata");
                int quantity = Config.crafting.getInt(key + ".quantity");
                List<String> enchants = Config.crafting.getStringList(key + ".enchantments");
                List<String> lores = Config.crafting.getStringList(key + ".lores");
                RecipeType recipeType = Config.crafting.getBoolean(key + ".shapelessRecipe") ? RecipeType.SHAPELESS_RECIPE : RecipeType.SHAPED_RECIPE;
                boolean deleteOthers = Config.crafting.getBoolean(key + ".deleteOthers");
                boolean transferDurability = Config.crafting.getBoolean(key + ".transferDurability");
                boolean usePermission = Config.crafting.getBoolean(key + ".usePermission");
                String customName = color(Config.crafting.getString(key + ".customName"));
                
                short metadata = 0;
                String permission = "ur.craft." + name;
                ItemStack shpedre;
                
                if (metad instanceof String && (toCraft == Material.SKULL_ITEM))
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
                
                applyCustomName(shpedre, customName);
                
                applyLores(shpedre, lores);
                
                applyEnchants(shpedre, enchants);
                
                Recipe recipe = new ShapedRecipe(shpedre);
                if (recipeType == RecipeType.SHAPELESS_RECIPE)
                {
                    recipe = new ShapelessRecipe(shpedre);
                }
                CustomRecipe custRecipe = new CustomRecipe();
                custRecipe.type = recipeType;
                custRecipe.name = name;
                custRecipe.usePermission = usePermission;
                custRecipe.permission = permission;
                custRecipe.deleteOthers = deleteOthers;
                custRecipe.transferDurability = transferDurability;
                if (recipeType == RecipeType.SHAPED_RECIPE)
                {
                    List<String> shape_list = Config.crafting.getStringList(key + ".recipe");
                    String[] shape = new String[shape_list.size()];
                    for (int i = 0; i < shape.length; i++)
                    {
                        shape[i] = shape_list.get(i);
                    }
                    ((ShapedRecipe)recipe).shape(shape);
                }
                
                Set<String> keys2 = Config.crafting.getConfigurationSection(key + ".ingredientsID").getKeys(false);
                for (String key2 : keys2)
                {
                    ConfigurationSection section2 = Config.crafting.getConfigurationSection(key + ".ingredientsID");
                    char c = key2.charAt(0);
                    byte metaIng = -1;
                    int quantityIng = 1;
                    Material materialIng;
                    String readed = section2.getString(key2);
                    if (RecipesManager.getCustomRecipeByName(readed) != null)
                    {
                        try
                        {
                            if (recipeType == RecipeType.SHAPED_RECIPE)
                            {
                                ((ShapedRecipe)recipe).setIngredient(c, RecipesManager.getCustomRecipeByName(readed).bukkitRecipe.getResult().getData());
                            }
                            else
                            {
                                ((ShapelessRecipe)recipe).addIngredient(RecipesManager.getCustomRecipeByName(readed).bukkitRecipe.getResult().getData());
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.severe("[UnlimitedRecipes] Error while adding bukkitRecipe for: " + toCraft.name() + ":" + metadata);
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        if (readed.contains(":") && readed.contains("x"))
                        {
                            metaIng = Byte.parseByte(readed.split(":")[1].split("x")[0]);
                            materialIng = getMaterial(readed.split(":")[0]);
                            quantityIng = Short.parseShort(readed.split("x")[1]);
                        }
                        else if (readed.contains(":"))
                        {
                            metaIng = Byte.parseByte(readed.split(":")[1]);
                            materialIng = getMaterial(readed.split(":")[0]);
                        }
                        else if (readed.contains("x"))
                        {
                            metaIng = Byte.parseByte(readed.split("x")[0]);
                            materialIng = getMaterial(readed.split(":")[0]);
                            quantityIng = Integer.parseInt(readed.split("x")[1]);
                        }
                        else
                        {
                            materialIng = getMaterial(readed);
                        }
                        try
                        {
                            if (recipeType == RecipeType.SHAPED_RECIPE)
                            {
                                ((ShapedRecipe)recipe).setIngredient(c, materialIng, metaIng);
                            }
                            else
                            {
                                ((ShapelessRecipe)recipe).addIngredient(quantityIng, materialIng, metaIng);
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.severe("[UnlimitedRecipes] Error while adding bukkitRecipe for: " + toCraft.name() + ":" + metadata);
                            e.printStackTrace();
                        }
                    }
                }
                custRecipe.bukkitRecipe = recipe;
                RecipesManager.registerRecipe(custRecipe);
                if(debug)
                {
                    LOG.info("[UnlimitedRecipes] Crafting Recipe for: " + toCraft.name() + ":" + metadata + " added !");
                }
            }
            LOG.info("[UnlimitedRecipes] All craft recipes loaded ! ("+keys.size() + " recipes)");
        }
    }
    
    public static void loadFurnaceRecipes()
    {
        if (Config.furnace.getConfigurationSection("config.smelts") != null)
        {
            Set<String> keys = Config.furnace.getConfigurationSection("config.smelts").getKeys(false);
            
            if(keys == null || keys.isEmpty())
            {
                return;
            }
            
            for (String name : keys)
            {
                String key = "config.smelts." + name;
                Material material = getMaterial(Config.furnace.getString(key + ".resultID"));
                byte metaResult = (byte)Config.furnace.getInt(key + ".result_MetaData");
                String customName = Config.furnace.getString(key + ".result_customName");
                List<String> lores = Config.furnace.getStringList(key + ".result_lores");
                Material ingredient = getMaterial(Config.furnace.getString(key + ".ingredientID"));
                Config.furnace.getInt(key + ".ingredient_MetaData");
                ItemStack shpedre = new ItemStack(material, 1, metaResult);
                applyCustomName(shpedre, customName);
                applyLores(shpedre, lores);
                FurnaceRecipe recipe = new FurnaceRecipe(shpedre, ingredient.getNewData(metaResult));
                RecipesManager.registerRecipe(new CustomRecipe(RecipeType.FURNACE_RECIPE, name, recipe, false, null, false, false));
                if(debug)
                {
                    LOG.info("[UnlimitedRecipes] Furnace Recipe for: " + material.name() + ":" + metaResult + " added !");
                }
            }
            LOG.info("[UnlimitedRecipes] All smelt recipes loaded ! ("+keys.size() + " recipes)");
        }
    }
    
    public static String color(String string)
    {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }
    
    private static void applyEnchants(ItemStack its, List<String> enchants)
    {
        if (enchants != null && !enchants.isEmpty())
        {
            for (String str : enchants)
            {
                its.addUnsafeEnchantment(getEnchantment(str.split(":")[0]), Integer.valueOf(str.split(":")[1]));
            }
        }
    }
    
    private static void applyLores(ItemStack its, List<String> lores)
    {
        if (lores != null && !lores.isEmpty())
        {
            List<String> lstmp = new ArrayList<String>();
            for (String s : lores)
            {
                lstmp.add(ChatColor.RESET + color(s));
            }
            ItemMeta tmp = its.getItemMeta();
            tmp.setLore(lstmp);
            its.setItemMeta(tmp);
        }
    }
    
    private static void applyCustomName(ItemStack its, String name)
    {
        if (name != null)
        {
            ItemMeta tmp = its.getItemMeta();
            tmp.setDisplayName(ChatColor.RESET + color(name));
            its.setItemMeta(tmp);
        }
    }
    
    private static Enchantment getEnchantment(String obj)
    {
        if(isInt(obj))
        {
            return Enchantment.getById(Integer.valueOf(obj));
        }
        else
        {
            return Enchantment.getByName(obj);
        }
    }
    
    public void saveCraftingConfig()
    {
        try
        {
            URPlugin.instance.getDataFolder().mkdirs();
            Config.crafting.save(craftingFile);
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
            Config.furnace.save(furnaceFile);
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
