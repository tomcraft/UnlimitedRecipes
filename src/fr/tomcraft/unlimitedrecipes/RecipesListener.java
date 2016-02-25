package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.SkullMeta;

import fr.tomcraft.unlimitedrecipes.URecipe.RecipeType;

public class RecipesListener implements Listener
{
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (UpdateThread.updateAvailable && URPlugin.hasPermission(e.getPlayer(), "ur.update"))
        {
            e.getPlayer().sendMessage(ChatColor.RED + "[UnlimitedRecipes] An update is available," + (UpdateThread.updateDownloading ? " it will be applied on next restart." : " you can get it here: "));
            e.getPlayer().sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/ (click)");
        }
    }
    
    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e)
    {
        Player p = (Player)e.getView().getPlayer();
        
        if(URPlugin.craftViewers.containsKey(p.getName()))
        {
            e.setCancelled(true);
            return;
        }
        
        if(!URPlugin.craftMaking.containsKey(p.getName()))
        {
            return;
        }
        
        URecipe uRecipe = URPlugin.craftMaking.get(p.getName());
        
        if(e.getSlot() == 17)
        {
            char[] az = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
            HashMap<ItemStack, Character> itsToChar = new HashMap<ItemStack, Character>();
            HashMap<Character, ItemStack> charToIts = new HashMap<Character, ItemStack>();
            
            CraftingInventory inv = (CraftingInventory)e.getView().getTopInventory();
            
            if(uRecipe.getType() == RecipeType.SHAPED_RECIPE)
            {
                ShapedRecipe recipe = new ShapedRecipe(p.getItemInHand());
                String[] shape = {"", "", ""};
                
                for(int i = 0; i < 9; i++)
                {
                    ItemStack its = inv.getMatrix()[i];
                    char c = its == null || its.getType() == Material.AIR ? ' ' : itsToChar.containsKey(its) ? itsToChar.get(its) : az[i];
                    itsToChar.put(its, c);
                    charToIts.put(c, its);
                    
                    shape[i/3] += c;           
                }
                
                ArrayList<String> shapeList = new ArrayList<String>(Arrays.asList(shape));
                for(int i=0;i<shapeList.size();i++)
                {
                    if(shapeList.get(i).trim().isEmpty())
                    {
                        shapeList.remove(i);
                        i--;
                    }
                }
                
                shapeList.trimToSize();
                
                for(int i=0;i<3;i++)
                {
                    boolean need = true;
                    for(String line : shapeList)
                    {
                        if(!line.startsWith(" "))
                        {
                            need = false;
                        }
                    }
                    if(need)
                    {
                        for(int j=0;j<shapeList.size();j++)
                        {
                            String line = shapeList.get(j);
                            shapeList.set(j, line.substring(1));
                        }
                    }
                    need = true;
                    for(String line : shapeList)
                    {
                        if(!line.endsWith(" "))
                        {
                            need = false;
                        }
                    }
                    if(need)
                    {
                        for(int j=0;j<shapeList.size();j++)
                        {
                            String line = shapeList.get(j);
                            shapeList.set(j, line.substring(0, line.length()-1));
                        }
                    }
                }
                
                shape = new String[shapeList.size()];
                recipe.shape(shapeList.toArray(shape));
                
                for(char cc : charToIts.keySet())
                {
                    if(cc == ' ')
                    {
                        continue;
                    }
                    ItemStack ing = charToIts.get(cc);
                    if(ing.getDurability() != -1)
                    {
                        recipe.setIngredient(cc, ing.getData());
                    }
                    else
                    {
                        recipe.setIngredient(cc, ing.getType());
                    }
                }
                
                uRecipe.setBukkitRecipe(recipe);
            }
            else if(uRecipe.getType() == RecipeType.SHAPELESS_RECIPE)
            {
                ShapelessRecipe recipe = new ShapelessRecipe(p.getItemInHand());
                
                int i = 0;
                for(ItemStack its : inv.getMatrix())
                {
                    if(its != null && its.getType() != Material.AIR && its != inv.getResult())
                    {
                        itsToChar.put(its, az[i]);
                        if(its.getDurability() != -1)
                        {
                            recipe.addIngredient(its.getAmount(), its.getData());
                        }
                        else
                        {
                            recipe.addIngredient(its.getAmount(), its.getType());
                        }
                        i++;
                    }
                }
                
                uRecipe.setBukkitRecipe(recipe);
            }
            uRecipe.load();
            Config.save();
            RecipesManager.reload();
            p.sendMessage(ChatColor.GREEN + "Success !");
            
            p.closeInventory();
            p.getInventory().setItem(17, null);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        Player p = (Player)e.getView().getPlayer();
        if(URPlugin.craftMaking.containsKey(p.getName()))
        {
            URPlugin.craftMaking.remove(p.getName());
        }
        if(URPlugin.craftViewers.containsKey(p.getName()))
        {
            e.getView().getTopInventory().clear();
            URPlugin.craftViewers.remove(p.getName());
        }
    }
    
    @EventHandler
    public void onPlayerCraftEvent(PrepareItemCraftEvent e)
    {
        if(URPlugin.craftMaking.containsKey(e.getView().getPlayer().getName()))
        {
            return;
        }
        
        Recipe recipe = e.getRecipe();
        ItemStack result = recipe.getResult();
        URecipe custRecipe = RecipesManager.getURecipeByRecipe(recipe);
        
        if (custRecipe != null)
        {
            if (custRecipe.enablePermission() && !URPlugin.hasPermission(e.getView().getPlayer(), custRecipe.getPermission()))
            {
                e.getInventory().setResult(null);
                return;
            }
            if (recipe.getResult().getType() == Material.SKULL_ITEM)
            {
                SkullMeta meta = (SkullMeta)result.getItemMeta();
                if(meta.getOwner().equalsIgnoreCase("%player%"))
                {
                    meta.setOwner(e.getView().getPlayer().getName());
                    result.setItemMeta(meta);
                    e.getInventory().setResult(result);
                }
            }
            if (custRecipe.transferDamage())
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
