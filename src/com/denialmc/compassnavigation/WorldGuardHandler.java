package com.denialmc.compassnavigation;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class WorldGuardHandler
{
  public WorldGuardPlugin worldGuard;
  
  public static class CompassFlag
    extends StateFlag
  {
    public static CompassFlag flag = new CompassFlag();
    
    public CompassFlag()
    {
      super("compass", true);
    }
    
    public static boolean setAllowsFlag(ApplicableRegionSet set)
    {
      return set.allows(flag);
    }
    
    public static void addFlag()
    {
      try
      {
        Field flags = DefaultFlag.class.getDeclaredField("flagsList");
        Field modifiers = Field.class.getDeclaredField("modifiers");
        
        modifiers.setAccessible(true);
        modifiers.setInt(flags, flags.getModifiers() & 0xFFFFFFEF);
        
        flags.setAccessible(true);
        
        List<Flag<?>> elements = new ArrayList(Arrays.asList(DefaultFlag.getFlags()));
        elements.add(flag);
        
        Flag[] list = new Flag[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
          list[i] = ((Flag)elements.get(i));
        }
        flags.set(null, list);
        
        Field regionManager = WorldGuardPlugin.class.getDeclaredField("globalRegionManager");
        regionManager.setAccessible(true);
        
        GlobalRegionManager globalManager = (GlobalRegionManager)regionManager.get(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
        
        globalManager.preload();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public WorldGuardHandler(CompassNavigation plugin)
  {
    this.worldGuard = ((WorldGuardPlugin)plugin.getServer().getPluginManager().getPlugin("WorldGuard"));
    CompassFlag.addFlag();
  }
  
  public boolean canUseCompassHere(Location location)
  {
    return CompassFlag.setAllowsFlag(this.worldGuard.getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(BukkitUtil.toVector(location)));
  }
}
