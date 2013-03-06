package fr.tomcraft.unlimitedrecipes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class RecipesListener implements Listener{

	public Main plugin;

	public RecipesListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerCraftEvent(CraftItemEvent e)
	{
		if(plugin.overidenCrafts.contains(e.getRecipe())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSmeltEvent(org.bukkit.event.inventory.FurnaceSmeltEvent e)
	{
		if(plugin.overidenSmelts.contains(e.getResult())){
			e.setCancelled(true);
		}
	}
	
}
