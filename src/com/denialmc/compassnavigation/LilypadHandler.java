package com.denialmc.compassnavigation;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResult;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.RedirectResult;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class LilypadHandler
{
  public CompassNavigation plugin;
  public boolean result = false;
  
  public LilypadHandler(CompassNavigation plugin)
  {
    this.plugin = plugin;
  }
  
  public boolean connect(Player player, String server)
  {
    this.result = false;
    try
    {
      Connect connect = (Connect)this.plugin.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
      connect.request(new RedirectRequest(server, player.getName())).registerListener(new FutureResultListener<RedirectResult>()
      {
        public void onResult(RedirectResult redirectResult)
        {
          LilypadHandler.this.result = (redirectResult.getStatusCode() == StatusCode.SUCCESS);
        }
      });
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return this.result;
  }
}
