package fr.tomcraft.unlimitedrecipes;

import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class URecipe
{
    
    private Recipe bukkitRecipe;
    private RecipeType type;
    private String name;
    private boolean enablePermission;
    private boolean disableOthers;
    private boolean transferDamage;
    
    private boolean loaded = false;
    
    public URecipe(Recipe bukkitRecipe, RecipeType type)
    {
        this.bukkitRecipe = bukkitRecipe;
        this.type = type;
    }
    
    public URecipe setBukkitRecipe(Recipe bukkitRecipe)
    {
        this.bukkitRecipe = bukkitRecipe;
        return this;
    }
    
    public Recipe getBukkitRecipe()
    {
        return bukkitRecipe;
    }
    
    public ItemStack getResult()
    {
        return getBukkitRecipe().getResult();
    }
    
    public RecipeType getType()
    {
        return type;
    }
    
    public URecipe setName(String name)
    {
        this.name = name;
        return this;
    }
    
    public String getName()
    {
        return name;
    }
    
    public URecipe setEnablePermission(boolean enable)
    {
        this.enablePermission = enable;
        return this;
    }
    
    public boolean enablePermission()
    {
        return enablePermission;
    }
    
    public URecipe setDisableOthers(boolean disable)
    {
        this.disableOthers = disable;
        return this;
    }
    
    public boolean disableOthers()
    {
        return disableOthers;
    }
    
    public URecipe setTransferDamage(boolean enable)
    {
        this.transferDamage = enable;
        return this;
    }
    
    public boolean transferDamage()
    {
        return transferDamage;
    }
    
    public String getPermission()
    {
        return "ur.craft." + getName();
    }
    
    public void load()
    {
        if(!loaded)
        {
            RecipesManager.customRecipes.put(getName(), this);
            loaded = true;  
        }
    }
    
    public enum RecipeType
    {
        SHAPED_RECIPE(ShapedRecipe.class), SHAPELESS_RECIPE(ShapelessRecipe.class), FURNACE_RECIPE(FurnaceRecipe.class);
        
        
        public Class<? extends Recipe> getType()
        {
            return type;
        }
        
        private Class<? extends Recipe> type;
        
        private RecipeType(Class<? extends Recipe> type)
        {
            this.type = type;
        }
        
        public static RecipeType fromName(String name)
        {
            for(RecipeType type : values())
            {
                if(type.name().toLowerCase().startsWith(name.toLowerCase()))
                {
                    return type;
                }
            }
            return null;
        }
    }
}
