package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.tomcraft.unlimitedrecipes.URecipe.RecipeType;
import fr.tomcraft.unlimitedrecipes.utils.UpdateThread;

public class RecipesListener implements Listener
{
    
    public static int saveSlot = 17;
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (UpdateThread.updateAvailable && e.getPlayer().hasPermission("ur.update"))
        {
            e.getPlayer().sendMessage(ChatColor.RED + "[UnlimitedRecipes] An update is available," + (UpdateThread.updateDownloading ? " it will be applied on next restart." : " you can get it here: "));
            e.getPlayer().sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/unlimitedrecipes/ (click)");
        }
    }
    
    @EventHandler
    public void onPlayerInventoryClick(final InventoryClickEvent e)
    {
        final Player p = (Player)e.getView().getPlayer();
        final Inventory inventory = e.getView().getTopInventory();
        
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
        
        if(e.getSlotType() == SlotType.RESULT)
        {
            ItemStack newResult = e.getCursor() != null && e.getCursor().getType() != Material.AIR ? e.getCursor().clone() : null;
            ItemStack newCursor = e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR ? e.getCurrentItem().clone() : null;
            
            if(newCursor != null && newResult != null && newResult.isSimilar(newCursor))
            {
                newResult.setAmount(newResult.getAmount() + newCursor.getAmount());
                newCursor = null;
            }
            
            p.setItemOnCursor(newCursor);
            inventory.setItem(e.getSlot(), newResult);
            if(Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE).contains(e.getAction()))
            {
                URPlugin.craftMakingResultTMP.put(p.getName(), newResult);
            }
            e.setCancelled(true);
            return;
        }
        
        if(e.getSlotType() == SlotType.FUEL)
        {
            inventory.setItem(e.getSlot(), e.getCurrentItem()); 
            e.setCancelled(true);
            return;
        }
        
        ItemStack result = null;
        
        if(e.getSlot() == saveSlot)
        {   
            result = uRecipe.getType() == RecipeType.FURNACE_RECIPE ? inventory.getItem(2) : inventory.getItem(0);
            
            if(result == null || result.getType() == Material.AIR)
            {
                p.sendMessage(ChatColor.RED + "Error: The result slot is empty !");
                e.setCancelled(true);
                return;
            }
            
            if(uRecipe.getType() == RecipeType.FURNACE_RECIPE)
            {
                FurnaceRecipe recipe = new FurnaceRecipe(result, inventory.getItem(0).getData());
                uRecipe.setBukkitRecipe(recipe);
            }
            else
            {
                char[] az = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
                HashMap<ItemStack, Character> itsToChar = new HashMap<ItemStack, Character>();
                HashMap<Character, ItemStack> charToIts = new HashMap<Character, ItemStack>();
                
                CraftingInventory inv = (CraftingInventory)e.getView().getTopInventory();
                
                if(uRecipe.getType() == RecipeType.SHAPED_RECIPE)
                {
                    ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(URPlugin.instance, uRecipe.getName()), result);
                    String[] shape = {"", "", ""};
                    
                    for(int i = 0; i < 9; i++)
                    {
                        ItemStack its = inv.getMatrix()[i];
                        char c = its == null || its.getType() == Material.AIR ? ' ' : itsToChar.containsKey(its) ? itsToChar.get(its) : az[i];
                        
                        if(c != ' ')
                        {
                            itsToChar.put(its, c);
                            charToIts.put(c, its);
                        }
                        
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
                    
                    try
                    {
                        recipe.shape(shapeList.toArray(shape));
                    }
                    catch(Exception ex)
                    {
                        p.sendMessage(ChatColor.RED + "Error: Invalid shape !");
                        e.setCancelled(true);
                        return;
                    }
                    
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
                    ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(URPlugin.instance, uRecipe.getName()), result);
                    
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
                    if(recipe.getIngredientList().isEmpty())
                    {
                        p.sendMessage(ChatColor.RED + "Error: Invalid recipe !");
                        e.setCancelled(true);
                        return;
                    }
                    uRecipe.setBukkitRecipe(recipe);
                }
            }
            
            uRecipe.load();
            Config.save();
            RecipesManager.reload();
            p.sendMessage(ChatColor.GREEN + "Saved recipe successfuly !");
            
            p.closeInventory();
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        Player p = (Player)e.getPlayer();
        if(URPlugin.craftMaking.containsKey(p.getName()))
        {
            if(e.getView().getTopInventory() instanceof FurnaceInventory)
            {
                e.getView().getTopInventory().setItem(1, null);
            }
            
            List<ItemStack> toGiveBack = Arrays.asList(e.getView().getTopInventory().getContents());
            
            while(toGiveBack.contains(null))
                toGiveBack.remove(null);
            
            e.getPlayer().getInventory().addItem(toGiveBack.toArray(new ItemStack[toGiveBack.size()]));
            e.getView().getTopInventory().clear();
            e.getPlayer().getInventory().setItem(saveSlot, null);
            URPlugin.craftMaking.remove(p.getName());
            URPlugin.craftMakingResultTMP.remove(p.getName());
        }
        if(URPlugin.craftViewers.containsKey(p.getName()))
        {
            e.getView().getTopInventory().clear();
            URPlugin.craftViewers.remove(p.getName());
        }
    }
    
    @EventHandler
    public void onPlayerCraftEvent(CraftItemEvent e)
    {
        if(URPlugin.craftMaking.containsKey(e.getView().getPlayer().getName()))
        {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerPrepareCraftEvent(final PrepareItemCraftEvent e)
    {
        if(URPlugin.craftMaking.containsKey(e.getView().getPlayer().getName()))
        {        
            return;
        }
        
        Recipe recipe = e.getRecipe();
        if(recipe == null)
        {
            return;
        }
        ItemStack result = recipe.getResult();
        URecipe custRecipe = RecipesManager.getURecipeByRecipe(recipe);
        
        if (custRecipe != null)
        {
            if (custRecipe.enablePermission() && !e.getView().getPlayer().hasPermission(custRecipe.getPermission()))
            {
                e.getInventory().setResult(null);
                return;
            }
            
            if(result.hasItemMeta())
            {
                String playerName = e.getView().getPlayer().getName();
                ItemMeta meta = result.getItemMeta();
                
                if(meta.hasDisplayName() && meta.getDisplayName().contains("%player%"))
                {
                    meta.setDisplayName(meta.getDisplayName().replace("%player%", playerName));
                }
                
                if(meta.hasLore())
                {
                    ArrayList<String> lores = new ArrayList<String>();
                    for(String lore : meta.getLore())
                    {
                        lores.add(lore.replace("%player%", playerName));
                    }
                    meta.setLore(lores);
                }
                
                if (recipe.getResult().getType() == Material.SKULL_ITEM)
                {
                    SkullMeta skullMeta = (SkullMeta)result.getItemMeta();
                    if(skullMeta.getOwner().equalsIgnoreCase("%player%"))
                    {
                        skullMeta.setOwner(playerName);
                    }
                }
                
                if(result.getItemMeta() != meta)
                {
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
