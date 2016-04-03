package fr.tomcraft.unlimitedrecipes;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ResfreshTask extends BukkitRunnable
{
    
    public ResfreshTask(Player player)
    {
        this.player = player;
    }

    private Player player;

    @Override
    public void run()
    {
        if(player == null || !player.isOnline() || !URPlugin.craftMaking.containsKey(player.getName()))
        {
            cancel();
            return;
        }
        player.getOpenInventory().getTopInventory().setItem(0, URPlugin.craftMakingResultTMP.get(player.getName()));
        player.updateInventory();
    }
    
}
