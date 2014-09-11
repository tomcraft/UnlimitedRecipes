package fr.tomcraft.unlimitedrecipes;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomRecipe
{

    public RecipeType type;
    public String name;
    public Recipe recipe;
    public boolean usePermission;
    public String permission;
    public boolean override;
    public boolean deleteOthers;
    public Map<Character, ItemStack> ingredients;

    public CustomRecipe()
    {
    }

    public CustomRecipe(RecipeType type, String name, Recipe recipe, boolean usePermission, String permission, boolean override, boolean deleteOthers)
    {
        this.type = type;
        this.name = name;
        this.recipe = recipe;
        this.usePermission = usePermission;
        this.permission = permission;
        this.override = override;
        this.deleteOthers = deleteOthers;
    }

    public enum RecipeType
    {
        SHAPED_RECIPE, SHAPELESS_RECIPE, FURNACE_RECIPE
    }
}
