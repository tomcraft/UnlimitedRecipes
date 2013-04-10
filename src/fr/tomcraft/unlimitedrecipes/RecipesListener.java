package fr.tomcraft.unlimitedrecipes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

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

			if(!plugin.customCrafts.contains(recipe.getIngredientMap()))
			{
				if(plugin.overidenCrafts.get(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()) != null)
				{
					ItemStack result = recipe.getResult();

					ItemStack custom = plugin.overidenCrafts.get(recipe.getResult().getTypeId()+":"+recipe.getResult().getDurability()).getResult();

					if(result.getType() == custom.getType() && result.getDurability() == custom.getDurability() && plugin.overidenCrafts.get(result.getTypeId()+":"+result.getDurability()).getShape() != recipe.getShape())
					{
						e.getInventory().remove(e.getRecipe().getResult());
					}

				}
			}
			else
			{
				System.out.println("1");
				if(!plugin.hasPermission(e.getView().getPlayer().getName(), "ur.craft.("+e.getRecipe().getResult().getTypeId()+":"+e.getRecipe().getResult().getDurability()+")"))
				{
					System.out.println("2");
					e.getInventory().remove(e.getRecipe().getResult());
				}
			}

		}
		else if(e.getRecipe() instanceof ShapelessRecipe)
		{

		}
	}

}
