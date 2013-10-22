package fr.tomcraft.unlimitedrecipes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.SkullMeta;

public class RecipesListener implements Listener{

	public Main plugin;

	public RecipesListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if(Updater.updateAvailable && plugin.hasPermission(e.getPlayer(), "ur.update"))
		{
			e.getPlayer().sendMessage(ChatColor.RED + "[UnlimitedRecipes] An update is available, you can get it here:");
			e.getPlayer().sendMessage(ChatColor.RED+"http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/ (click)");
		}
	}

	@EventHandler
	public void onPlayerCraftEvent(PrepareItemCraftEvent e)
	{
		if(e.getRecipe() instanceof ShapedRecipe)
		{
			ShapedRecipe recipe = ((ShapedRecipe)e.getRecipe());
			String resultS = recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability();
			if(!plugin.isCustomRecipe(recipe) && e.getInventory().getResult() != null)
			{
				
				if(plugin.getCustomRecipeByResult(resultS) != null && plugin.getCustomRecipeByResult(resultS).deleteOthers)
				{
					ItemStack result = recipe.getResult();

					ItemStack custom = plugin.getCustomRecipeByResult(resultS).recipe.getResult();

					if(!result.isSimilar(custom) || result.getItemMeta() != custom.getItemMeta())
					{
						e.getInventory().setResult(null);
						if(plugin.getCustomRecipeByResult(resultS).override)
						{
							this.checkCustomShapedRecipeValid(e, recipe, resultS);
						}
					}

				}
			}
			else
			{
				this.checkCustomShapedRecipeValid(e, recipe, resultS);
			}

		}
		else if(e.getRecipe() instanceof ShapelessRecipe)
		{
			ShapelessRecipe recipe = ((ShapelessRecipe)e.getRecipe());
			if(!plugin.isCustomRecipe(recipe))
			{
				if(plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()) != null)
				{
					ItemStack result = recipe.getResult();

					ItemStack custom = plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe.getResult();

					if(result.getType() == custom.getType() && result.getDurability() == custom.getDurability() && ((ShapelessRecipe)plugin.getCustomRecipeByResult(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).recipe).getIngredientList() != recipe.getIngredientList())
					{
						e.getInventory().setResult(null);
					}

				}
			}
			else
			{
				if(plugin.getCustomRecipeByRecipe(recipe).usePermission && !plugin.hasPermission(e.getView().getPlayer().getName(), plugin.getCustomRecipeByRecipe(recipe).permission))
				{
					e.getInventory().setResult(null);
					return;
				}

				if(recipe.getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)recipe.getResult().getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
				{
					ItemStack result = recipe.getResult();
					SkullMeta meta = ((SkullMeta)result.getItemMeta());
					meta.setOwner(e.getView().getPlayer().getName());
					result.setItemMeta(meta);
					e.getInventory().setResult(result);
				}
			}
		}
	}
	
	public void checkCustomShapedRecipeValid(PrepareItemCraftEvent e, ShapedRecipe recipe, String resultS)
	{
		if(e.getInventory().getResult() == null)
		{
			e.getInventory().setResult(plugin.getCustomRecipeByResult(resultS).recipe.getResult());
		}
		
		if(plugin.getCustomRecipeByResult(resultS).usePermission && !plugin.hasPermission(e.getView().getPlayer().getName(), plugin.getCustomRecipeByName(resultS).permission))
		{
			e.getInventory().setResult(null);
			return;
		}

		if(recipe.getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)recipe.getResult().getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
		{
			ItemStack result = recipe.getResult();
			SkullMeta meta = ((SkullMeta)result.getItemMeta());
			meta.setOwner(e.getView().getPlayer().getName());
			result.setItemMeta(meta);
			e.getInventory().setResult(result);
		}
	}
}
