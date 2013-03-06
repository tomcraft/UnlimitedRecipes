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
		if(!plugin.customCrafts.contains(e.getRecipe()))
		{
			if(plugin.overidenCrafts.contains(e.getRecipe().getResult()))
				e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSmeltEvent(org.bukkit.event.inventory.FurnaceSmeltEvent e)
	{
		if(!plugin.customSmelts.contains(e.getResult()))
		{
			if(plugin.overidenSmelts.get(e.getResult()) != e.getSource())
				e.setCancelled(true);
		}
	}
	
}
