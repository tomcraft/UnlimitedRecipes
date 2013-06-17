package fr.tomcraft.unlimitedrecipes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class CustomRecipe {

	public Main plugin;

	public Recipe recipe;

	public boolean usePermission;

	public String permission;

	public boolean override;

	public CustomRecipe()
	{

	}

	public CustomRecipe(Recipe recipe, Main plugin, boolean usePermission, String permission, boolean override)
	{
		this.plugin = plugin;
		this.recipe = recipe;
		this.usePermission = usePermission;
		this.permission = permission;
		this.override = override;
	}

	public void register()
	{
		this.plugin.customRecipes.add(this);
		
		/*if(recipe instanceof ShapedRecipe)
		{
			addToCraftingManagerShaped();
		}*/
		
		this.plugin.getServer().addRecipe(recipe);
	}

	public void addToCraftingManagerShaped() {
		try{
			Object[] data;
			String[] shape = ((ShapedRecipe)recipe).getShape();
			Map<Character, ItemStack> ingred = ((ShapedRecipe)recipe).getIngredientMap();
			int datalen = shape.length;
			datalen += ingred.size() * 2;
			int i = 0;
			data = new Object[datalen];
			for (; i < shape.length; i++) {
				data[i] = shape[i];
			}
			for (char c : ingred.keySet()) {
				ItemStack mdata = ingred.get(c);
				if (mdata == null) continue;
				data[i] = c;
				i++;
				int id = mdata.getTypeId();
				short dmg = mdata.getDurability();
				int amount = mdata.getAmount();
				data[i] = Class.forName(Main.PACKAGE_NAME_SERVER+".ItemStack").getConstructor(int.class, int.class, int.class).newInstance(id, amount, dmg);
				i++;
			}
			
			Class<?> c = Class.forName(Main.PACKAGE_NAME_SERVER+".CraftingManager");
			Class<?> ci = Class.forName(Main.PACKAGE_NAME_CRAFTBUKKIT+".inventory.CraftItemStack");
			Method m = c.getDeclaredMethod("registerShapedRecipe", Class.forName(Main.PACKAGE_NAME_SERVER+".ItemStack"), Object[].class);
			Method mi = ci.getDeclaredMethod("asNMSCopy", ItemStack.class);
			Object its = mi.invoke(ci, ((ShapedRecipe)recipe).getResult());
			System.out.println(its);
			System.out.println(Arrays.asList(data));
			m.invoke(c, its, data);
			//CraftingManager.getInstance().registerShapedRecipe((net.minecraft.server.v1_5_R3.ItemStack) its, data);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
