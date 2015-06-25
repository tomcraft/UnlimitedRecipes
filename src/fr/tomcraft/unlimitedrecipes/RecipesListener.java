package fr.tomcraft.unlimitedrecipes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.SkullMeta;

public class RecipesListener implements Listener
{
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (UpdateThread.updateAvailable && URPlugin.hasPermission(e.getPlayer(), "ur.update"))
        {
            e.getPlayer().sendMessage(ChatColor.RED + "[UnlimitedRecipes] An update is available," + (UpdateThread.updateDownloading ? " it will be applied on next restart." : " you can get it here: "));
            e.getPlayer().sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/ (click)");
        }
    }
    
    @EventHandler
    public void onPlayerCraftEvent(PrepareItemCraftEvent e)
    {
        Recipe recipe = e.getRecipe();
        ItemStack result = recipe.getResult();
        CustomRecipe custRecipe = null;
        if ((custRecipe = RecipesManager.getCustomRecipeByRecipe(recipe)) != null)
        {
            if (custRecipe.usePermission && !URPlugin.hasPermission(e.getView().getPlayer(), custRecipe.permission))
            {
                e.getInventory().setResult(null);
                return;
            }
            if (recipe.getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)result.getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
            {
                SkullMeta meta = (SkullMeta)result.getItemMeta();
                meta.setOwner(e.getView().getPlayer().getName());
                result.setItemMeta(meta);
                e.getInventory().setResult(result);
            }
            if (custRecipe.transferDurability)
            {
                float rendment = 1F;
                for (ItemStack its : e.getInventory().getMatrix())
                {
                    if (its != null && its != e.getInventory().getResult() && !its.getType().isBlock() && its.getType() != Material.INK_SACK && its.getType().getMaxDurability() != 0)
                    {
                        float displayDura = its.getType().getMaxDurability() - its.getDurability();
                        rendment = rendment * (displayDura / its.getType().getMaxDurability());
                    }
                }
                short newDurability = (short)(rendment * result.getType().getMaxDurability());
                ItemStack newResult = result.clone();
                newResult.setDurability((short)(result.getType().getMaxDurability() - newDurability));
                e.getInventory().setResult(newResult);
            }
        }
    }
}
