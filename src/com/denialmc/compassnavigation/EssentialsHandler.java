package com.denialmc.compassnavigation;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.api.IWarps;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class EssentialsHandler
{
  public IWarps warps;
  
  public EssentialsHandler(CompassNavigation plugin)
  {
    this.warps = ((IEssentials)plugin.getServer().getPluginManager().getPlugin("Essentials")).getWarps();
  }
  
  public Location getWarp(String warp)
  {
    try
    {
      this.warps.reloadConfig();
      return this.warps.getWarp(warp);
    }
    catch (Exception e) {}
    return null;
  }
}
