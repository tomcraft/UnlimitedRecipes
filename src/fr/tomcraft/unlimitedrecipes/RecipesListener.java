package fr.tomcraft.unlimitedrecipes;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.SkullMeta;

public class RecipesListener implements Listener{

	public Main plugin;

	public RecipesListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerCraftEvent(PrepareItemCraftEvent e)
	{
		if(e.getRecipe() instanceof ShapedRecipe)
		{
			ShapedRecipe recipe = ((ShapedRecipe)e.getRecipe());

			if(!plugin.isCustomRecipe(recipe))
			{
				System.out.println("noCustShaped");
				if(plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()) != null && plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).override)
				{
					ItemStack result = recipe.getResult();

					ItemStack custom = plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe.getResult();

					if(result.getType() == custom.getType() && result.getDurability() == custom.getDurability() && !((ShapedRecipe)plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe).getShape().equals(recipe.getShape()))
					{
						e.getInventory().setResult(null);
						//e.getInventory().remove(e.getRecipe().getResult());
					}

				}
			}
			else
			{
				System.out.println(plugin.getCustomRecipeByRecipe(recipe).permission);
				if(plugin.getCustomRecipeByRecipe(recipe).usePermission && !plugin.hasPermission(e.getView().getPlayer().getName(), plugin.getCustomRecipeByRecipe(recipe).permission))
				{
					System.out.println("2");
					e.getInventory().setResult(null);
					return;
				}

				if(recipe.getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)recipe.getResult().getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
				{
					ItemStack result = recipe.getResult();
					SkullMeta meta = ((SkullMeta)result.getItemMeta());
					meta.setOwner(e.getView().getPlayer().getName());
					result.setItemMeta(meta);
					//e.getInventory().remove(recipe.getResult());
					e.getInventory().setResult(result);
				}
			}

		}
		else if(e.getRecipe() instanceof ShapelessRecipe)
		{
			ShapelessRecipe recipe = ((ShapelessRecipe)e.getRecipe());
			System.out.println("3");
			if(!plugin.isCustomRecipe(recipe))
			{
				System.out.println("2");
				if(plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()) != null)
				{
					ItemStack result = recipe.getResult();

					ItemStack custom = plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe.getResult();

					if(result.getType() == custom.getType() && result.getDurability() == custom.getDurability() && ((ShapelessRecipe)plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe).getIngredientList() != recipe.getIngredientList())
					{
						e.getInventory().setResult(null);
						//e.getInventory().remove(e.getRecipe().getResult());
					}

				}
			}
			else
			{
				if(plugin.getCustomRecipeByRecipe(recipe).usePermission && !plugin.hasPermission(e.getView().getPlayer().getName(), plugin.getCustomRecipeByRecipe(recipe).permission))
				{
					System.out.println("2");
					e.getInventory().setResult(null);
					return;
				}

				if(recipe.getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)recipe.getResult().getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
				{
					ItemStack result = recipe.getResult();
					SkullMeta meta = ((SkullMeta)result.getItemMeta());
					meta.setOwner(e.getView().getPlayer().getName());
					result.setItemMeta(meta);
					//e.getInventory().remove(recipe.getResult());
					e.getInventory().setResult(result);
				}
			}
		}
	}
	
}
