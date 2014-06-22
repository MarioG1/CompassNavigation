package com.denialmc.compassnavigation;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class VaultHandler
{
  public Economy economy;
  
  public VaultHandler(CompassNavigation plugin)
  {
    RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
    if (provider != null) {
      this.economy = ((Economy)provider.getProvider());
    }
  }
}
