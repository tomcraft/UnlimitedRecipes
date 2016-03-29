package fr.tomcraft.unlimitedrecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import fr.tomcraft.unlimitedrecipes.URecipe.RecipeType;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class URPlugin extends JavaPlugin
{
    
    public static URPlugin instance;
    public static Updater updater;
    
    public static HashMap<String, URecipe> craftMaking = new HashMap<String, URecipe>();
    public static HashMap<String, URecipe> craftViewers = new HashMap<String, URecipe>();
    
    @Override
    public void onEnable()
    {
        URPlugin.instance = this;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(new RecipesListener(), this);
    }
    
    public FileConfiguration getCraftingConfig()
    {
        return Config.crafting;
    }
    
    public FileConfiguration getFurnaceConfig()
    {
        return Config.furnace;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length == 0 || args[0].equalsIgnoreCase("help"))
        {
            int help = args.length >= 2 ? Integer.parseInt(args[1]) : 1;
            sender.sendMessage(ChatColor.GOLD + "---------- UnlimitedRecipes Help page "+help + "/4 ----------");
            sender.sendMessage(ChatColor.GOLD + "Commands needs you to have the RESULT item in your hand");
            if(help == 1)
            {
                sender.sendMessage(ChatColor.RED + "Usage: /ur reload");
                sender.sendMessage(ChatColor.RED + "Usage: /ur create <name> <shaped/shapeless> [enablePermission] [disableOthers] [transfertDurability]");
                sender.sendMessage(ChatColor.RED + "Usage: /ur create <name> furnace <input[:data]> [disableOthers]");
                sender.sendMessage(ChatColor.RED + "Usage: /ur list");
                sender.sendMessage(ChatColor.RED + "Usage: /ur view <name>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur delete <name>");
            }
            else if(help == 2)
            {
                sender.sendMessage(ChatColor.GRAY + "In a text put %player% to include the player name");
                sender.sendMessage(ChatColor.GRAY + "In a text put _ to make a space");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item rename <name>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item lore add <line>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item lore reset");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item enchant add <enchant> <level>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item enchant list");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item enchant reset");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item unbreakable <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item skull <ownerName or %player%>");
            }
            else if(help == 3)
            {
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide attributes <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide destroys <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide enchants <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide potion_effects <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide placed_on <true/false>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur item flag hide unbreakable <true/false>");
            }
            else if(help == 4)
            {
                sender.sendMessage(ChatColor.RED + "Usage: /ur blacklist <on/off>");
                sender.sendMessage(ChatColor.RED + "Usage: /ur blacklist add [useData] (true or false)");
                sender.sendMessage(ChatColor.RED + "Usage: /ur blacklist delete");
                sender.sendMessage(ChatColor.RED + "Usage: /ur blacklist list");
            }
            
            return false;
        }
        String action = args[0];
        if(action.equalsIgnoreCase("reload") && hasPermission(sender, "ur.reload"))
        {
            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded !");
            return true;
        }
        else if(action.equalsIgnoreCase("list") && hasPermission(sender, "ur.list"))
        {
            sender.sendMessage(ChatColor.GREEN + "List of loaded recipes:");
            for(String name : RecipesManager.customRecipes.keySet())
            {
                sender.sendMessage(ChatColor.GREEN + " - " + name);
            }
            sender.sendMessage(ChatColor.GREEN + "Total: " + RecipesManager.customRecipes.size());
            return true;
        }
        else if(action.equalsIgnoreCase("view") && args.length >= 2 && hasPermission(sender, "ur.view"))
        {
            Player p = (Player)sender;
            URecipe uRecipe = RecipesManager.customRecipes.get(args[1]);
            if(uRecipe == null)
            {
                p.sendMessage(ChatColor.RED + "Incorrect recipe name !");
                return false;
            }
            Recipe recipe = uRecipe.getBukkitRecipe();
            
            if(uRecipe.getType() == RecipeType.FURNACE_RECIPE)
            {
                p.sendMessage(ChatColor.RED + "Smelt "+ChatColor.GREEN+((FurnaceRecipe)recipe).getInput().getType().name().toLowerCase() + ChatColor.RED + ".");
                return true;
            }
            InventoryView iv = p.openWorkbench(p.getLocation(), true);
            CraftingInventory inv = ((CraftingInventory)iv.getTopInventory());
            craftViewers.put(p.getName(), uRecipe);
            
            if(recipe instanceof ShapelessRecipe) 
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
            } else 
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
            }
            return true;
        }
        else if(action.equalsIgnoreCase("delete") && args.length >= 2 && hasPermission(sender, "ur.delete"))
        {
            RecipesManager.customRecipes.remove(args[1]);
            Config.save();
            RecipesManager.reload();
            sender.sendMessage(ChatColor.GREEN + "Recipe deleted !");
            return true;
        }
        else if(action.equalsIgnoreCase("blacklist") && args.length >= 2 && hasPermission(sender, "ur.blacklist"))
        {
            action = args[1];
            boolean useData = args.length >= 3 ? Boolean.parseBoolean(args[2]) : true;
            Player p = (Player)sender;
            ItemStack item = p.getItemInHand();
            ArrayList<String> blackList = RecipesManager.blacklist;
            
            if(action.equalsIgnoreCase("on"))
            {
                getConfig().set("enableBlackList", true);
                sender.sendMessage(ChatColor.GREEN + "Done");
            }
            else if(action.equalsIgnoreCase("off"))
            {
                getConfig().set("enableBlackList", false);
                sender.sendMessage(ChatColor.GREEN + "Done");
            }
            else if(action.equalsIgnoreCase("add"))
            {
                blackList.add(item.getType() + (useData ? ":" + item.getData().getData() : ""));
                sender.sendMessage(ChatColor.GREEN + "Recipe blacklisted !");
            }
            else if(action.equalsIgnoreCase("remove"))
            {
                for(String s : new ArrayList<String>(blackList))
                {
                    Material mat = Config.getMaterial(s.split(":")[0]);
                    byte data = s.contains(":") ? Byte.parseByte(s.split(":")[1]) : -1;
                    
                    if(mat != item.getType() || s.contains(":") && data != item.getData().getData())
                    {
                        continue;
                    }
                    
                    blackList.remove(s);
                }
                sender.sendMessage(ChatColor.GREEN + "Recipe unblacklisted !");
            }
            else if(action.equalsIgnoreCase("list"))
            {
                for(String s : blackList)
                {
                    p.sendMessage(ChatColor.GREEN + " - " + s);
                }
                sender.sendMessage(ChatColor.GREEN + "Done");
            }
            
            RecipesManager.blacklist = blackList;
            Config.saveBlacklist();
            
            RecipesManager.reload();
            return true;
        }
        else if(action.equalsIgnoreCase("hide") && args.length >= 3 && hasPermission(sender, "ur.hide"))
        {
            action = args[1];
            boolean hide = args.length >= 4 ? Boolean.parseBoolean(args[3]) : true;
            
            RecipesManager.getURecipeByName(args[2]).setHiden(hide);
            
            Config.save();
            RecipesManager.reload();
            sender.sendMessage(ChatColor.GREEN + "Done");
            return true;
        }
        else if(action.equalsIgnoreCase("item") && args.length >= 2 && hasPermission(sender, "ur.item"))
        {
            action = args[1];
            Player p = (Player)sender;
            ItemStack item = p.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            if(action.equalsIgnoreCase("rename") && args.length >= 3)
            {
                String name = ChatColor.translateAlternateColorCodes('&', args[2].replace("_", " "));
                meta.setDisplayName(name);
            }
            else if(action.equalsIgnoreCase("lore") && args.length >= 4 && args[2].equalsIgnoreCase("add"))
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
            else if(action.equalsIgnoreCase("lore") && args.length >= 3 && args[2].equalsIgnoreCase("reset"))
            {
                meta.setLore(new ArrayList<String>());
            }
            else if(action.equalsIgnoreCase("enchant") && args.length >= 4 && args[2].equalsIgnoreCase("add"))
            {
                Enchantment e = Enchantment.getByName(args[3].toUpperCase());
                if(e == null)
                {
                    p.sendMessage(ChatColor.RED + "Invalid enchantment !");
                    return false;
                }
                int level = Integer.parseInt(args[4]);
                if(meta instanceof EnchantmentStorageMeta)
                {
                    ((EnchantmentStorageMeta)meta).addStoredEnchant(e, level, true);
                }
                else
                {
                    meta.addEnchant(e, level, true);
                }
            }
            else if(action.equalsIgnoreCase("enchant") && args.length >= 3 && args[2].equalsIgnoreCase("list"))
            {
                for(Enchantment e : Enchantment.values())
                {
                    p.sendMessage(ChatColor.GREEN + " - " + e.getName() + " [" + e.getStartLevel() + "-" + e.getMaxLevel() + "]");
                }
            }
            else if(action.equalsIgnoreCase("enchant") && args.length >= 3 && args[2].equalsIgnoreCase("reset"))
            {
                for(Enchantment e : meta.getEnchants().keySet())
                {
                    meta.removeEnchant(e);
                }
            }
            else if(action.equalsIgnoreCase("unbreakable") && args.length >= 3)
            {
                meta.spigot().setUnbreakable(Boolean.parseBoolean(args[2]));
            }
            else if(action.equalsIgnoreCase("skull") && args.length >= 3)
            {
                String name = args[2];
                if(meta instanceof SkullMeta)
                {
                    ((SkullMeta)meta).setOwner(name);
                }
            }
            else if(action.equalsIgnoreCase("hide") && args.length >= 4)
            {
                ItemFlag flag = ItemFlag.valueOf("HIDE_"+args[2].toUpperCase());
                if(flag != null)
                {
                    boolean state = Boolean.parseBoolean(args[3]);
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
                p.sendMessage(ChatColor.GREEN + "Item in you hand was updated !");
                return true;
            }            
        }
        else if(action.equalsIgnoreCase("create") && args.length >= 3 && hasPermission(sender, "ur.create"))
        {
            Player p = (Player)sender;
            ItemStack result = p.getItemInHand();
            
            if(result == null || result.getType() == Material.AIR)
            {
                p.sendMessage(ChatColor.RED + "You must have the result item in your hand !");
                return false;
            }
            
            RecipeType type = RecipeType.fromName(args[2]);
            URecipe recipe = new URecipe(null, type);
            recipe.setName(args[1]);
            
            if(type != RecipeType.FURNACE_RECIPE)
            {
                recipe.setEnablePermission(args.length >= 4 ? Boolean.parseBoolean(args[3]) : false);
                recipe.setDisableOthers(args.length >= 5 ? Boolean.parseBoolean(args[4]) : false);
                recipe.setTransferDamage(args.length >= 6 ? Boolean.parseBoolean(args[5]) : false);
                craftMaking.put(p.getName(), recipe);
                
                ItemStack wool = new ItemStack(Material.WOOL, 1, DyeColor.LIME.getWoolData());
                ItemMeta meta = wool.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "Save recipe");
                meta.setLore(Arrays.asList(ChatColor.RED + "You must have the result item in your hand !"));
                wool.setItemMeta(meta);
                p.getInventory().setItem(17, wool);
                
                p.openWorkbench(p.getLocation(), true);
            }
            else if(args.length >= 4)
            {
                String content = args[3];
                Material material = Config.getMaterial(content.contains(":") ? content.split(":")[0] : content);
                short data = Short.parseShort((content.contains(":") ? content.split(":")[1] : "-1"));
                ItemStack input = new ItemStack(material, 1, data);
                
                FurnaceRecipe bukkitRecipe = new FurnaceRecipe(result, input.getData());
                
                if(data == -1)
                {
                    bukkitRecipe = new FurnaceRecipe(result, material);
                }
                
                recipe.setBukkitRecipe(bukkitRecipe);
                recipe.setDisableOthers(args.length >= 5 ? Boolean.parseBoolean(args[4]) : false);
                recipe.setEnablePermission(false);
                recipe.setTransferDamage(false);
                
                recipe.load();
                Config.save();
                RecipesManager.reload();
            }
            
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Command syntax error, use /ur help");
        return false;
    }
    
    @Override
    public void reloadConfig()
    {
        super.reloadConfig();
        RecipesManager.reset();
        Config.load();
        UpdateThread.restart();
    }
    
    public static boolean hasPermission(CommandSender sender, String perm)
    {
        return sender.hasPermission(perm) || sender.isOp();
    }
    
    public static void renewUpdater()
    {
        URPlugin.updater = new Updater(URPlugin.instance, 52907, URPlugin.instance.getFile(), UpdateThread.updateDownloading ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD, false);
    }
}