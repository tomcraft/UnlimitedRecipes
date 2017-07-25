package fr.tomcraft.unlimitedrecipes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import fr.tomcraft.unlimitedrecipes.Config;
import fr.tomcraft.unlimitedrecipes.RecipesManager;
import fr.tomcraft.unlimitedrecipes.URPlugin;
import fr.tomcraft.unlimitedrecipes.URecipe;
import fr.tomcraft.unlimitedrecipes.utils.CommandController.SubCommandHandler;

public class Tools
{
    private URPlugin plugin = URPlugin.instance;
    
    @SubCommandHandler(name = "list", parent = "ur", permission = "ur.list")
    public void list(CommandSender sender, String args[])
    {
        sender.sendMessage(ChatColor.GREEN + "List of loaded recipes:");
        for(String name : RecipesManager.customRecipes.keySet())
        {
            sender.sendMessage(ChatColor.GREEN + " - " + name);
        }
        sender.sendMessage(ChatColor.GREEN + "Total: " + RecipesManager.customRecipes.size());
    }
    
    @SubCommandHandler(name = "delete", parent = "ur", permission = "ur.delete")
    public void delete(CommandSender sender, String args[])
    {
        if(args.length == 0)
        {
            Help.showUsages(sender, "/ur delete");
            return;
        }
        RecipesManager.customRecipes.remove(args[0]);
        Config.save();
        RecipesManager.reload();
        sender.sendMessage(ChatColor.GREEN + "Recipe deleted !");
    }
    
    @SubCommandHandler(name = "reload", parent = "ur", permission = "ur.reload")
    public void reload(CommandSender sender, String args[])
    {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Config reloaded !");
    }
    
    @SubCommandHandler(name = "view", parent = "ur", permission = "ur.view")
    public void view(final Player player, String args[])
    {
        if(args.length == 0)
        {
            Help.showUsages(player, "/ur view");
            return;
        }
        URecipe uRecipe = RecipesManager.customRecipes.get(args[0]);
        if(uRecipe == null)
        {
            player.sendMessage(ChatColor.RED + "Invalid recipe name !");
            return;
        }
        Recipe recipe = uRecipe.getBukkitRecipe();
        InventoryView iv = recipe instanceof FurnaceRecipe ? player.openInventory(Bukkit.createInventory(player, InventoryType.FURNACE)) : player.openWorkbench(player.getLocation(), true);
        Inventory inv = iv.getTopInventory();
        URPlugin.craftViewers.put(player.getName(), uRecipe);
        
        if(recipe instanceof FurnaceRecipe)
        {
            FurnaceRecipe frecipe = (FurnaceRecipe)recipe;
            inv.setItem(0, frecipe.getInput());
            inv.setItem(2, frecipe.getResult());
            inv.setItem(1, new ItemStack(Material.COAL));
        }
        else if(recipe instanceof ShapelessRecipe) 
        {
            ShapelessRecipe sls = (ShapelessRecipe)recipe;
            int i = 1;
            for(ItemStack item : sls.getIngredientList()) 
            {
                if(item != null)
                {
                    ItemStack fixedItem = item.clone();
                    if(fixedItem.getDurability() == Short.MAX_VALUE)
                    {
                        fixedItem.setDurability((short)0);
                    }
                    inv.setItem(i, fixedItem);
                }
                i++;
            }
            inv.setItem(0, recipe.getResult());
        }
        else 
        {
            ShapedRecipe sd = (ShapedRecipe)recipe;
            int y = 0;
            int z = 1;
            for(String s : sd.getShape()) 
            {
                while(!s.equals("")) 
                {
                    ItemStack item = sd.getIngredientMap().get(s.charAt(0));
                    if(item != null)
                    {
                        ItemStack fixedItem = item.clone();
                        if(fixedItem.getDurability() == Short.MAX_VALUE)
                        {
                            fixedItem.setDurability((short)0);
                        }
                        inv.setItem(z+y, fixedItem);
                    }
                    z++;
                    s = s.substring(1);
                }
                z = 1;
                y = y+3;
            }
            inv.setItem(0, recipe.getResult());
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.updateInventory(), 15);
    }
    
}
