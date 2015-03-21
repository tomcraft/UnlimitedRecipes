package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import fr.tomcraft.unlimitedrecipes.CustomRecipe.RecipeType;

public class RecipesManager
{

    public static ArrayList<CustomRecipe> customRecipes = new ArrayList<CustomRecipe>();
    public static ArrayList<Map<Character, ItemStack>> customShapedCrafts = new ArrayList<Map<Character, ItemStack>>();
    
    public static void reset()
    {
        Bukkit.resetRecipes();
        customRecipes = new ArrayList<CustomRecipe>();
        customShapedCrafts = new ArrayList<Map<Character, ItemStack>>();
    }

    public static void registerRecipe(CustomRecipe recipe)
    {
        if (recipe.type == RecipeType.SHAPED_RECIPE)
        {
            recipe.ingredients = ((ShapedRecipe)recipe.recipe).getIngredientMap();
            RecipesManager.customShapedCrafts.add(recipe.ingredients);
        }
        RecipesManager.customRecipes.add(recipe);
        Bukkit.addRecipe(recipe.recipe);
    }

    public static CustomRecipe getCustomRecipeByRecipe(Recipe recipe)
    {
        for (CustomRecipe cust : RecipesManager.customRecipes)
        {
            if(!recipe.getResult().equals(cust.recipe.getResult()))
            {
                continue;
            }
            
            if (recipe instanceof ShapedRecipe && cust.type == RecipeType.SHAPED_RECIPE)
            {                
                return cust;
            }
            else if (recipe instanceof ShapelessRecipe && cust.type == RecipeType.SHAPELESS_RECIPE)
            {
                ShapelessRecipe custRecipe = ((ShapelessRecipe)cust.recipe);
                ShapelessRecipe bukkitRecipe = ((ShapelessRecipe)recipe);
                
                if(custRecipe.getIngredientList().size() == bukkitRecipe.getIngredientList().size() && custRecipe.getIngredientList().containsAll(bukkitRecipe.getIngredientList()))
                {
                    return cust;
                }
            }
            else if (recipe instanceof FurnaceRecipe && cust.type == RecipeType.FURNACE_RECIPE)
            {
                if (((FurnaceRecipe)cust.recipe).getInput().equals(((FurnaceRecipe)recipe).getInput()))
                {
                    return cust;
                }
            }
        }
        return null;
    }

    public static CustomRecipe getCustomRecipeByResult(String result)
    {
        for (CustomRecipe cust : RecipesManager.customRecipes)
        {
            ItemStack its = cust.recipe.getResult();
            if ((its.getType().name() + ":" + its.getDurability()).equals(result))
            {
                return cust;
            }
        }
        return null;
    }

    public static CustomRecipe getCustomRecipeByName(String name)
    {
        for (CustomRecipe cust : RecipesManager.customRecipes)
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
    }
}
