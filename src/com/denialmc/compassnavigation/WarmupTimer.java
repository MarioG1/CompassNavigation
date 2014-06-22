package com.denialmc.compassnavigation;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WarmupTimer
  extends BukkitRunnable
{
  public CompassNavigation plugin;
  public Player player;
  public String inventory;
  public int slot;
  
  public WarmupTimer(CompassNavigation plugin, Player player, String inventory, int slot)
  {
    this.plugin = plugin;
    this.player = player;
    this.inventory = inventory;
    this.slot = slot;
  }
  
  public void run()
  {
    this.plugin.timers.remove(this.player.getName());
    if ((this.player != null) && (this.player.isOnline())) {
      this.plugin.checkMoney(this.player, this.inventory, this.slot);
    }
  }
}
