package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipesManager
{
    
    public static HashMap<String, URecipe> customRecipes = new HashMap<String, URecipe>();
    public static ArrayList<String> blacklist = new ArrayList<String>();
    
    public static void reset()
    {
        Bukkit.resetRecipes();
        RecipesManager.customRecipes.clear();
    }
    
    public static void reload()
    {
        Bukkit.resetRecipes();
        registerBlacklist();
        registerRecipes();
    }
    
    public static void registerBlacklist()
    {
        for (String item : blacklist)
        {
            Material mat = null;
            Short data = null;
            mat = Config.getMaterial(item.split(":")[0]);
            data = item.contains(":") ? Short.parseShort(item.split(":")[1]) : null;
            RecipesManager.unloadBukkitRecipes(mat, data);
            if (Config.debug)
            {
                Config.LOG.info("[UnlimitedRecipes] All recipes for " + mat.name() + ":" + data + " were deleted !");
            }
        }
        Config.LOG.info("[UnlimitedRecipes] Recipes were deleted ! (" + blacklist.size() + " items)");
    }
    
    public static void registerRecipes()
    {
        for(URecipe recipe : customRecipes.values())
        {
            if(recipe.disableOthers())
            {
                ItemStack its = recipe.getResult();
                unloadBukkitRecipes(its.getType(), its.getDurability());
                if(Config.debug)
                {
                    Config.LOG.info("[UnlimitedRecipes] Disabled all recipes for: " + its.getType() + ":" + its.getDurability() + " !");
                }
            }
        }
        for(URecipe recipe : customRecipes.values())
        {
            Bukkit.addRecipe(recipe.getBukkitRecipe());
        }
        
        Config.LOG.info("[UnlimitedRecipes] All recipes were loaded ! (" + customRecipes.size() + " recipes)");
    }
    
    public static void unloadBukkitRecipes(Material material, Short data)
    {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        Recipe recipe;
        while (it.hasNext())
        {
            recipe = it.next();
            ItemStack item = recipe.getResult();
            if (recipe != null && item.getType() == material && (data != null ? item.getDurability() == data : true))
            {
                it.remove();
            }
        }
    }
    
    
    public static URecipe getURecipeByRecipe(Recipe recipe)
    {
        for (URecipe custRecipe : RecipesManager.customRecipes.values())
        {
            if(custRecipe.getBukkitRecipe().equals(recipe))
            {
                return custRecipe;
            }
            
            if(!custRecipe.getType().getType().isAssignableFrom(recipe.getClass()))
            {
                continue;
            }
            
            if (!recipe.getResult().isSimilar(custRecipe.getResult()))
            {
                continue;
            }
            
            if (recipe instanceof ShapelessRecipe)
            {
                ShapelessRecipe uRecipe = (ShapelessRecipe)custRecipe.getBukkitRecipe();
                ShapelessRecipe bukkitRecipe = (ShapelessRecipe)recipe;
                if (uRecipe.getIngredientList().size() != bukkitRecipe.getIngredientList().size())
                {
                    continue;
                }
                if (!uRecipe.getIngredientList().containsAll(bukkitRecipe.getIngredientList()))
                {
                    continue;
                }
            }
            else if (recipe instanceof FurnaceRecipe)
            {
                FurnaceRecipe uRecipe = (FurnaceRecipe)custRecipe.getBukkitRecipe();
                FurnaceRecipe bukkitRecipe = (FurnaceRecipe)recipe;
                if (!uRecipe.getInput().isSimilar(bukkitRecipe.getInput()))
                {
                    continue;
                }
            }
            
            return custRecipe;
        }
        return null;
    }
    
    public static URecipe getURecipeByName(String name)
    {
        return customRecipes.get(name);
    }
    
    /*
    public static URecipe getCustomRecipeByResult(String result)
    {
        for (URecipe cust : RecipesManager.customRecipes)
        {
            ItemStack its = cust.bukkitRecipe.getResult();
            if ((its.getType().name() + ":" + its.getDurability()).equals(result))
            {
                return cust;
            }
        }
        return null;
    }
    
    public static URecipe getCustomRecipeByName(String name)
    {
        for (URecipe cust : RecipesManager.customRecipes)
        {
            if (cust.name.equalsIgnoreCase(name))
            {
                return cust;
            }
        }
        return null;
    }
    
    public static boolean isCustomRecipe(Recipe recipe)
    {
        return RecipesManager.getCustomRecipeByRecipe(recipe) != null;
    }*/
}
