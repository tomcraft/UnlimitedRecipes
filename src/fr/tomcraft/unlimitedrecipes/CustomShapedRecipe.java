package fr.tomcraft.unlimitedrecipes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CustomShapedRecipe extends CustomRecipe{
	
	public Map<Character, ItemStack> ingredients = new HashMap<Character, ItemStack>();;
	
	public CustomShapedRecipe()
	{
		
	}

	public CustomShapedRecipe(String name, ShapedRecipe recipe, Main plugin,
			boolean usePermission, String permission, boolean override) {
		super(name, recipe, plugin, usePermission, permission, override);
	}

}
