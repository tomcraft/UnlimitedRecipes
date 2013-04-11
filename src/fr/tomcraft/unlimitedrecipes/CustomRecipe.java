package fr.tomcraft.unlimitedrecipes;

import org.bukkit.inventory.Recipe;

public class CustomRecipe {
	
	public Main plugin;
	
	public Recipe recipe;
	
	public boolean usePermission;
	
	public String permission;
	
	public boolean override;
	
	public CustomRecipe()
	{
		
	}
	
	public CustomRecipe(Recipe recipe, Main plugin, boolean usePermission, String permission, boolean override)
	{
		this.plugin = plugin;
		this.recipe = recipe;
		this.usePermission = usePermission;
		this.permission = permission;
		this.override = override;
	}
	
	public void register()
	{
		this.plugin.customRecipes.add(this);
		this.plugin.getServer().addRecipe(recipe);
	}

}
