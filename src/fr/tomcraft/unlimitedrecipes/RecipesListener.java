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
        String resultS = recipe.getResult().getTypeId() + ":" + recipe.getResult().getDurability();
        CustomRecipe custRecipe = null;
        
        if (RecipesManager.isCustomRecipe(recipe))
        {
            custRecipe = RecipesManager.getCustomRecipeByRecipe(recipe);
            
            if(custRecipe.usePermission && !URPlugin.hasPermission(e.getView().getPlayer().getName(), custRecipe.permission))
            {
                e.getInventory().setResult(null);
                return;
            }

            if (e.getRecipe().getResult().getType() == Material.SKULL_ITEM && ((SkullMeta)e.getRecipe().getResult().getItemMeta()).getOwner().equalsIgnoreCase("--CrafterHead"))
            {
                SkullMeta meta = (SkullMeta)result.getItemMeta();
                meta.setOwner(e.getView().getPlayer().getName());
                result.setItemMeta(meta);
                e.getInventory().setResult(result);
            }
        }
        else
        {
            custRecipe = RecipesManager.getCustomRecipeByResult(resultS);
            if (custRecipe != null && custRecipe.deleteOthers)
            {
                ItemStack custom = custRecipe.recipe.getResult();

                if (!result.isSimilar(custom) || result.getItemMeta() != custom.getItemMeta())
                {
                    e.getInventory().setResult(null);
                    if (custRecipe.override)
                    {
                        e.getInventory().setResult(custRecipe.recipe.getResult());
                    }
                }
            }
        }

    }


}
