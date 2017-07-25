package fr.tomcraft.unlimitedrecipes.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.tomcraft.unlimitedrecipes.URPlugin;
import fr.tomcraft.unlimitedrecipes.utils.CommandController.SubCommandHandler;

public class ItemConfig
{
    private URPlugin plugin = URPlugin.instance;
    private static List<String> subCommands = Arrays.asList("rename", "lore", "enchant", "potion", "unbreakable", "skull", "flag");
    
    @SubCommandHandler(name = "item", parent = "ur", permission = "ur.item")
    public void itemConfig(Player player, String args[])
    {
        if(args.length == 0 || !subCommands.contains(args[0].toLowerCase()))
        {
            Help.showUsages(player, "/ur item");
            return;
        }
        
        String action = args[0];
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        
        if(action.equalsIgnoreCase("rename") && args.length >= 2)
        {
            String name = ChatColor.translateAlternateColorCodes('&', args[1].replace("_", " "));
            meta.setDisplayName(name);
        }
        else if(action.equalsIgnoreCase("lore"))
        {
            lore(player, URPlugin.subArgs(args), meta);
        }
        else if(action.equalsIgnoreCase("enchant"))
        {
            enchant(player, URPlugin.subArgs(args), meta);
        }
        else if(action.equalsIgnoreCase("potion"))
        {
            potion(player, URPlugin.subArgs(args), meta);
        }
        else if(action.equalsIgnoreCase("unbreakable") && args.length >= 2)
        {
            meta.spigot().setUnbreakable(Boolean.parseBoolean(args[1]));
        }
        else if(action.equalsIgnoreCase("skull") && args.length >= 2)
        {
            String name = args[1];
            if(meta instanceof SkullMeta)
            {
                ((SkullMeta)meta).setOwner(name);
            }
        }
        else if(action.equalsIgnoreCase("hide") && args.length >= 3)
        {
            ItemFlag flag = ItemFlag.valueOf("HIDE_"+args[1].toUpperCase());
            if(flag != null)
            {
                boolean state = Boolean.parseBoolean(args[2]);
                if(state)
                {
                    meta.addItemFlags(flag);
                }
                else
                {
                    meta.removeItemFlags(flag);
                }
            }
        }
        
        if(meta != item.getItemMeta())
        {
            item.setItemMeta(meta);
            player.sendMessage(ChatColor.GREEN + "Item in you hand has been updated !");
            return;
        }            
    }
    
    public void lore(Player player, String args[], ItemMeta meta)
    {
        if(args.length == 0 || !Arrays.asList("add", "reset").contains(args[0].toLowerCase()))
        {
            Help.showUsages(player, "/ur item lore");
            return;
        }
        else if(args[0].equalsIgnoreCase("add") && args.length == 1)
        {
            Help.showUsages(player, "/ur item lore add");
            return;
        }
        
        String action = args[0];
        
        if(action.equalsIgnoreCase("add"))
        {
            List<String> list = meta.getLore();
            if(list == null)
            {
                list = new ArrayList<String>();
            }
            
            String line = ChatColor.translateAlternateColorCodes('&', args[3].replace("_", " "));
            list.add(line);
            
            meta.setLore(list);
        }
        else if(action.equalsIgnoreCase("reset"))
        {
            meta.setLore(new ArrayList<String>());
        }
    }
    
    public void enchant(Player player, String args[], ItemMeta meta)
    {
        if(args.length == 0 || !Arrays.asList("add", "list", "reset").contains(args[0].toLowerCase()))
        {
            Help.showUsages(player, "/ur item enchant");
            return;
        }
        else if(args[0].equalsIgnoreCase("add") && args.length < 3)
        {
            Help.showUsages(player, "/ur item enchant add");
            return;
        }
        
        String action = args[0];
        
        if(action.equalsIgnoreCase("add"))
        {
            Enchantment e = Enchantment.getByName(args[1].toUpperCase());
            if(e == null)
            {
                player.sendMessage(ChatColor.RED + "Invalid enchantment !");
                return;
            }
            int level = Integer.parseInt(args[2]);
            if(meta instanceof EnchantmentStorageMeta)
            {
                ((EnchantmentStorageMeta)meta).addStoredEnchant(e, level, true);
            }
            else
            {
                meta.addEnchant(e, level, true);
            }
        }
        else if(action.equalsIgnoreCase("list"))
        {
            for(Enchantment e : Enchantment.values())
            {
                player.sendMessage(ChatColor.GREEN + " - " + e.getName() + " [" + e.getStartLevel() + "-" + e.getMaxLevel() + "]");
            }
        }
        else if(action.equalsIgnoreCase("reset"))
        {
            for(Enchantment e : meta.getEnchants().keySet())
            {
                meta.removeEnchant(e);
            }
        }
    }
    
    public void potion(Player player, String args[], ItemMeta meta)
    {
        if(args.length == 0 || !Arrays.asList("add", "list", "reset").contains(args[0].toLowerCase()))
        {
            Help.showUsages(player, "/ur item potion");
            return;
        }
        else if(args[0].equalsIgnoreCase("add") && args.length < 3)
        {
            Help.showUsages(player, "/ur item potion add");
            return;
        }
        else if(!(meta instanceof PotionMeta))
        {
            player.sendMessage(ChatColor.RED + "Item must be potion effect-able !");
            return;
        }
        
        String action = args[0];
        
        if(action.equalsIgnoreCase("add"))
        {
            PotionEffectType e = PotionEffectType.getByName(args[1].toUpperCase());
            if(e == null)
            {
                player.sendMessage(ChatColor.RED + "Invalid potion !");
                return;
            }
            int level = Integer.parseInt(args[2]) + 1;
            int duration = Integer.parseInt(args[3]);
            PotionEffect effect = new PotionEffect(e, duration, level);
            ((PotionMeta)meta).addCustomEffect(effect, true);
        }
        else if(action.equalsIgnoreCase("list"))
        {
            for(PotionEffectType e : PotionEffectType.values())
            {
                player.sendMessage(ChatColor.GREEN + " - " + e.getName());
            }
        }
        else if(action.equalsIgnoreCase("reset"))
        {
            for(PotionEffect e : ((PotionMeta)meta).getCustomEffects())
            {
                ((PotionMeta)meta).removeCustomEffect(e.getType());
            }
        }
    }
}
