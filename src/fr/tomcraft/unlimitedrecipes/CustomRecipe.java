package fr.tomcraft.unlimitedrecipes;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomRecipe
{

    public RecipeType type;
    public String name;
    public Recipe bukkitRecipe;
    public boolean usePermission;
    public String permission;
    public boolean deleteOthers;
    public boolean transferDurability;
    public Map<Character, ItemStack> ingredients;

    public CustomRecipe()
    {
    }

    public CustomRecipe(RecipeType type, String name, Recipe recipe, boolean usePermission, String permission, boolean override, boolean deleteOthers)
    {
        this.type = type;
        this.name = name;
        this.bukkitRecipe = recipe;
        this.usePermission = usePermission;
        this.permission = permission;
        this.deleteOthers = deleteOthers;
    }

    public enum RecipeType
    {
        SHAPED_RECIPE, SHAPELESS_RECIPE, FURNACE_RECIPE
    }
}
