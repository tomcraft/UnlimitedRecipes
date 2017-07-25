package fr.tomcraft.unlimitedrecipes.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.tomcraft.unlimitedrecipes.RecipesListener;
import fr.tomcraft.unlimitedrecipes.URPlugin;
import fr.tomcraft.unlimitedrecipes.URecipe;
import fr.tomcraft.unlimitedrecipes.URecipe.RecipeType;
import fr.tomcraft.unlimitedrecipes.utils.CommandController.SubCommandHandler;
import fr.tomcraft.unlimitedrecipes.utils.ResfreshTask;

public class Create
{
    private URPlugin plugin = URPlugin.instance;
    
    @SubCommandHandler(name = "create", parent = "ur", permission = "ur.create")
    public void create(final Player player, String args[])
    {
        if(args.length < 2)
        {
            Help.showUsages(player, "/ur create");
            return;
        }
        
        RecipeType type = RecipeType.fromName(args[1]);
        
        if(type == null)
        {
            player.sendMessage(ChatColor.RED + "Syntax error: '" + args[1] + "' is not a valid recipe type !");
            Help.showUsages(player, "/ur create");
            return;
        }
        
        URecipe recipe = new URecipe(null, type);
        recipe.setName(args[0]);
        
        recipe.setDisableOthers(args.length >= 3 ? Boolean.parseBoolean(args[2]) : false);
        recipe.setEnablePermission(args.length >= 4 ? Boolean.parseBoolean(args[3]) : false);
        recipe.setTransferDamage(args.length >= 5 ? Boolean.parseBoolean(args[4]) : true);
        URPlugin.craftMaking.put(player.getName(), recipe);
        
        ItemStack wool = new ItemStack(Material.WOOL, 1, DyeColor.LIME.getWoolData());
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Save recipe");
        meta.setLore(Arrays.asList(ChatColor.RED + "The result slot could not be empty !"));
        wool.setItemMeta(meta);
        player.getInventory().setItem(RecipesListener.saveSlot, wool);
        
        if(type != RecipeType.FURNACE_RECIPE)
        {
            player.openWorkbench(player.getLocation(), true);
            new ResfreshTask(player).runTaskTimer(plugin, 0, 1);
        }
        else
        {
            Inventory inv = Bukkit.createInventory(player, InventoryType.FURNACE);
            inv.setItem(0, null);
            inv.setItem(1, new ItemStack(Material.COAL));
            player.openInventory(inv);
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.updateInventory(), 15);
    }
}
