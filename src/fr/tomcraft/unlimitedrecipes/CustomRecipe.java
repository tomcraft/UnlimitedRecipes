package fr.tomcraft.unlimitedrecipes;

import org.bukkit.inventory.Recipe;

public class CustomRecipe {

	public Main plugin;

	public String name;

	public Recipe recipe;

	public boolean usePermission;

	public String permission;

	public boolean override;
	
	public boolean deleteOthers;

	public CustomRecipe()
	{

	}

	public CustomRecipe(String name, Recipe recipe, Main plugin, boolean usePermission, String permission, boolean override, boolean deleteOthers)
	{
		this.name = name;
		this.plugin = plugin;
		this.recipe = recipe;
		this.usePermission = usePermission;
		this.permission = permission;
		this.override = override;
		this.deleteOthers = deleteOthers;
	}

	public void register()
	{
		this.plugin.customRecipes.add(this);

		/*if(recipe instanceof ShapedRecipe)
		{
			try {
				addToCraftingManagerShaped();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}*/

		this.plugin.getServer().addRecipe(recipe);
	}

	/*public void addToCraftingManagerShaped() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException {
		ShapedRecipe sr = (ShapedRecipe)recipe;

		ArrayList<Object> trucs = new ArrayList<Object>();


		for(String s : sr.getShape())
		{
			if(s != null)
				trucs.add(s);
		}

		for(char c : ((CustomShapedRecipe)this).ingredients.keySet())
		{
			if(((CustomShapedRecipe)this).ingredients.get(c) != null){
				trucs.add(c);
				trucs.add(toNMSItemStack(this, ((CustomShapedRecipe)this).ingredients.get(c)));
			}
		}
		System.out.println(trucs);
		Object nmsIts = toNMSItemStack(this, recipe.getResult());

		Method toCall = Class.forName(Main.PACKAGE_NAME_SERVER+".CraftingManager").getMethod("registerShapedRecipe", Class.forName(Main.PACKAGE_NAME_SERVER+".ItemStack"), Object[].class);

		List tmp = CraftingManager.getInstance().recipes;

		tmp.add(toCall.invoke(toCall.getDeclaringClass().newInstance(), nmsIts, trucs.toArray()));

		CraftingManager.getInstance().recipes = tmp; //
		Method toCallAfter = Class.forName(Main.PACKAGE_NAME_SERVER+".CraftingManager").getMethod("sort");

		toCallAfter.invoke(toCallAfter.getDeclaringClass().newInstance());



		//	CraftingManager.getInstance().registerShapedRecipe(CraftItemStack.asNMSCopy(recipe.getResult()), trucs.toArray());
	}


	public static Object toNMSItemStack(Object invoker, org.bukkit.inventory.ItemStack bukkitIts) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		return Class.forName(Main.PACKAGE_NAME_CRAFTBUKKIT+".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(invoker, bukkitIts);
	}*/

}
