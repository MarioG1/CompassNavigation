package com.denialmc.compassnavigation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

public class CompassNavigation
  extends JavaPlugin
  implements Listener
{
  public WorldGuardHandler worldGuardHandler;
  public ProtocolLibHandler protocolLibHandler;
  public VaultHandler vaultHandler;
  public AutoUpdater autoUpdater;
  public EssentialsHandler essentialsHandler;
  public LilypadHandler lilypadHandler;
  public HashMap<String, String> inventories = new HashMap();
  public HashMap<String, WarmupTimer> timers = new HashMap();
  
  public void onEnable()
  {
    getConfig().options().copyDefaults(true);
    saveConfig();
    if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
      this.protocolLibHandler = new ProtocolLibHandler(this);
    }
    if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
      this.essentialsHandler = new EssentialsHandler(this);
    }
    if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
      this.worldGuardHandler = new WorldGuardHandler(this);
    }
    if (getServer().getPluginManager().isPluginEnabled("Vault")) {
      this.vaultHandler = new VaultHandler(this);
    }
    if (getServer().getPluginManager().isPluginEnabled("LilyPad-Connect")) {
      this.lilypadHandler = new LilypadHandler(this);
    }
    if ((!getDescription().getVersion().contains("SNAPSHOT")) && (getConfig().getBoolean("settings.autoUpdate"))) {
      this.autoUpdater = new AutoUpdater(this, getFile(), true);
    }
    getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  public boolean canUseCompass(Player player)
  {
    if ((getConfig().getStringList("settings.disabledWorlds").contains(player.getWorld().getName())) && (!player.hasPermission("compassnav.useanywhere"))) {
      return false;
    }
    if ((this.worldGuardHandler != null) && (!player.hasPermission("compassnav.useanywhere"))) {
      return this.worldGuardHandler.canUseCompassHere(player.getLocation());
    }
    return true;
  }
  
  public int getId(String id)
  {
    return Integer.parseInt(id.split(":")[0]);
  }
  
  public short getDamage(String id)
  {
    String[] split = id.split(":");
    if (split.length >= 2) {
      return Short.parseShort(split[1]);
    }
    return 0;
  }
  
  public String replaceModifiers(Player player, String string)
  {
    return string.replace("<name>", player.getName()).replace("<displayname>", player.getDisplayName()).replace("<x>", Integer.toString(player.getLocation().getBlockX())).replace("<y>", Integer.toString(player.getLocation().getBlockY())).replace("<z>", Integer.toString(player.getLocation().getBlockZ())).replace("<yaw>", Integer.toString((int)player.getLocation().getYaw())).replace("<pitch>", Integer.toString((int)player.getLocation().getPitch()));
  }
  
  public String replacePlaceholders(String string)
  {
    return string;
  }
  
  public ItemStack setName(ItemStack item, String name)
  {
    try
    {
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      item.setItemMeta(meta);
      return item;
    }
    catch (Exception e) {}
    return item;
  }
  
  public ItemStack setLore(ItemStack item, ArrayList<String> lore)
  {
    try
    {
      ItemMeta meta = item.getItemMeta();
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
    }
    catch (Exception e) {}
    return item;
  }
  
  public ItemStack handleItem(Player player, String inventory, int slot)
  {
    if (getConfig().contains("settings." + inventory + slot)) {
      try
      {
        ArrayList<String> lore = new ArrayList();
        String name = null;
        if (getConfig().contains("settings." + inventory + slot + ".name")) {
          name = replacePlaceholders(ChatColor.RESET + getConfig().getString(new StringBuilder("settings.").append(inventory).append(slot).append(".name").toString()));
        }
        String item = getConfig().getString("settings." + inventory + slot + ".id", "2");
        for (String line : getConfig().getStringList("settings." + inventory + slot + ".lore")) {
          lore.add(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(line)));
        }
        if (!player.hasPermission(new Permission("compassnav." + inventory + slot, PermissionDefault.TRUE))) {
          lore.add(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(getConfig().getString("settings.noPermLore"))));
        }
        ItemStack stack = setLore(setName(new ItemStack(getId(item), getConfig().getInt("settings." + inventory + slot + ".amount", 1), getDamage(item)), name), lore);
        if ((this.protocolLibHandler != null) && (getConfig().getBoolean("settings." + inventory + slot + ".enchanted", false))) {
          stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 4);
        }
        if (this.protocolLibHandler != null) {
          return Util.removeAttributes(stack);
        }
        return stack;
      }
      catch (Exception e)
      {
        getLogger().warning("Couldn't set item '" + slot + "' in inventory '" + inventory + "' This is because of a wrongly set up config.");
        getLogger().warning("Technical exception: " + e.getMessage());
        getLogger().warning("More info on how to set it up correctly on http://goo.gl/sXdl3A");
      }
    }
    return null;
  }
  
  public void openInventory(Player player, String name)
  {
    if (getConfig().contains("settings." + name))
    {
      if (player.hasPermission(new Permission("compassnav." + name, PermissionDefault.TRUE))) {
        try
        {
          Inventory inventory = getServer().createInventory(null, getConfig().getInt("settings." + name + ".rows") * 9, replacePlaceholders(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings." + name + ".title").replace("<player>", player.getName()))));
          for (int slot = 0; slot < inventory.getSize() + 1; slot++)
          {
            ItemStack item = handleItem(player, name + ".", slot);
            if (item != null) {
              inventory.setItem(slot - 1, item);
            }
          }
          this.inventories.put(player.getName(), name + ".");
          player.openInventory(inventory);
        }
        catch (Exception e)
        {
          getLogger().severe("Couldn't open inventory '" + name + "'. This is because of a wrongly set up config.");
          getLogger().severe("Technical exception: " + e.getMessage());
          getLogger().severe("More info on how to set it up correctly on http://goo.gl/sXdl3A");
        }
      }
    }
    else
    {
      getLogger().severe("You do not have the inventory '" + name + "' set up, please set it up in the config.");
      getLogger().severe("More info on how to set it up on http://goo.gl/sXdl3A");
    }
  }
  
  public void checkUsage(Player player, String inventory, int slot)
  {
    if (canUseCompass(player)) {
      checkPlayers(player, inventory, slot);
    } else {
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.cantUseMessage")));
    }
  }
  
  public void checkPlayers(Player player, String inventory, int slot)
  {
    if (this.timers.containsKey(player.getName())) {
      ((WarmupTimer)this.timers.get(player.getName())).cancel();
    }
    if ((getConfig().getBoolean("settings.warmUp")) && (!player.hasPermission("compassnav.nowarmup")) && (!getConfig().contains("settings." + inventory + slot + ".inventory")))
    {
      boolean delay = false;
      for (Player p : player.getWorld().getPlayers()) {
        if ((p.getName() != player.getName()) && (p.getLocation().distance(player.getLocation()) < getConfig().getInt("settings.warmUpDistance")))
        {
          delay = true;
          break;
        }
      }
      if (delay)
      {
        this.timers.put(player.getName(), new WarmupTimer(this, player, inventory, slot));
        ((WarmupTimer)this.timers.get(player.getName())).runTaskLater(this, 20L * getConfig().getInt("settings.warmUpTime"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.warmUpMessage").replace("<time>", Integer.toString(getConfig().getInt("settings.warmUpTime")))));
      }
      else
      {
        checkMoney(player, inventory, slot);
      }
    }
    else
    {
      checkMoney(player, inventory, slot);
    }
  }
  
  public void checkMoney(Player player, String inventory, int slot)
  {
    if ((this.vaultHandler != null) && (this.vaultHandler.economy != null) && (!player.hasPermission("compassnav.free")))
    {
      if (getConfig().contains("settings." + inventory + slot + ".price"))
      {
        if (this.vaultHandler.economy.has(player.getName(), getConfig().getDouble("settings." + inventory + slot + ".price")))
        {
          this.vaultHandler.economy.withdrawPlayer(player.getName(), getConfig().getDouble("settings." + inventory + slot + ".price"));
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.chargedMessage").replace("<price>", Double.toString(getConfig().getDouble("settings." + inventory + slot + ".price")))));
          executeCommands(player, inventory, slot);
        }
        else
        {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.notEnoughMessage")));
          player.closeInventory();
        }
      }
      else {
        executeCommands(player, inventory, slot);
      }
    }
    else {
      executeCommands(player, inventory, slot);
    }
  }
  
  public void executeCommands(Player player, String inventory, int slot)
  {
    long delay = 1L;
    for (String command : getConfig().getStringList("settings." + inventory + slot + ".commands"))
    {
      final Player temp_p = player;
      final String temp_c = command;
      if (command.startsWith("c:")) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
          public void run()
          {
            CompassNavigation.this.getServer().dispatchCommand(CompassNavigation.this.getServer().getConsoleSender(), CompassNavigation.this.replaceModifiers(temp_p, temp_c.substring(2)));
          }
        }, delay);
      } else {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
          public void run()
          {
            CompassNavigation.this.getServer().dispatchCommand(temp_p, CompassNavigation.this.replaceModifiers(temp_p, temp_c));
          }
        }, delay);
      }
      delay += getConfig().getLong("settings.commandexecutedelay");
    }
    checkMessages(player, inventory, slot);
  }
  
  public void checkMessages(Player player, String inventory, int slot)
  {
    for (String message : getConfig().getStringList("settings." + inventory + slot + ".messages")) {
      player.sendMessage(replacePlaceholders(ChatColor.translateAlternateColorCodes('&', message.replace("<name>", player.getName()).replace("<displayname>", player.getDisplayName()))));
    }
    checkInventory(player, inventory, slot);
  }
  
  public void checkInventory(Player player, String inventory, int slot)
  {
    if (getConfig().contains("settings." + inventory + slot + ".inventory"))
    {
      player.closeInventory();
      openInventory(player, getConfig().getString("settings." + inventory + slot + ".inventory"));
    }
    else
    {
      checkBungee(player, inventory, slot);
    }
  }
  
  public void checkBungee(Player player, String inventory, int slot)
  {
    if (getConfig().contains("settings." + inventory + slot + ".bungee")) {
      try
      {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(byteOutput);
        output.writeUTF("Connect");
        output.writeUTF(getConfig().getString("settings." + inventory + slot + ".bungee"));
        
        player.closeInventory();
        player.sendPluginMessage(this, "BungeeCord", byteOutput.toByteArray());
      }
      catch (Exception e)
      {
        checkLilypad(player, inventory, slot);
      }
    } else {
      checkLilypad(player, inventory, slot);
    }
  }
  
  public void checkLilypad(Player player, String inventory, int slot)
  {
    if (this.lilypadHandler != null)
    {
      if (getConfig().contains("settings." + inventory + slot + ".lilypad"))
      {
        player.closeInventory();
        if (!this.lilypadHandler.connect(player, getConfig().getString("settings." + inventory + slot + ".lilypad"))) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.cantConnectMessage")));
        }
      }
      else
      {
        checkEssentials(player, inventory, slot);
      }
    }
    else {
      checkEssentials(player, inventory, slot);
    }
  }
  
  public void checkEssentials(Player player, String inventory, int slot)
  {
    if (this.essentialsHandler != null)
    {
      if (getConfig().contains("settings." + inventory + slot + ".warp"))
      {
        Location location = this.essentialsHandler.getWarp(getConfig().getString("settings." + inventory + slot + ".warp"));
        if (location != null)
        {
          player.teleport(location);
          player.closeInventory();
        }
        else
        {
          checkLocation(player, inventory, slot);
        }
      }
      else
      {
        checkLocation(player, inventory, slot);
      }
    }
    else {
      checkLocation(player, inventory, slot);
    }
  }
  
  public void checkLocation(Player player, String inventory, int slot)
  {
    if ((getConfig().contains("settings." + inventory + slot + ".world")) && (getConfig().contains("settings." + inventory + slot + ".x")) && (getConfig().contains("settings." + inventory + slot + ".y")) && (getConfig().contains("settings." + inventory + slot + ".z")) && (getConfig().contains("settings." + inventory + slot + ".yaw")) && (getConfig().contains("settings." + inventory + slot + ".pitch")))
    {
      player.closeInventory();
      player.teleport(new Location(getServer().getWorld(getConfig().getString("settings." + inventory + slot + ".world")), (float)getConfig().getDouble("settings." + inventory + slot + ".x"), (float)getConfig().getDouble("settings." + inventory + slot + ".y"), (float)getConfig().getDouble("settings." + inventory + slot + ".z"), (float)getConfig().getDouble("settings." + inventory + slot + ".yaw"), (float)getConfig().getDouble("settings." + inventory + slot + ".pitch")));
    }
    else
    {
      player.closeInventory();
    }
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    Player player = event.getPlayer();
    if ((event.hasItem()) && (event.getAction() == Action.RIGHT_CLICK_AIR))
    {
      ItemStack item = event.getItem();
      if (getConfig().contains("settings.items." + item.getTypeId()))
      {
        openInventory(player, getConfig().getString("settings.items." + item.getTypeId()));
        event.setCancelled(true);
        return;
      }
      if (getConfig().contains("settings.items." + item.getTypeId() + ":" + item.getDurability()))
      {
        openInventory(player, getConfig().getString("settings.items." + item.getTypeId() + ":" + item.getDurability()));
        event.setCancelled(true);
        return;
      }
    }
    else if ((event.hasItem()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK))
    {
      Block block = event.getClickedBlock();
      if ((!(block.getState() instanceof Sign)) || (!event.isCancelled()))
      {
        ItemStack item = event.getItem();
        if (getConfig().contains("settings.items." + item.getTypeId()))
        {
          openInventory(player, getConfig().getString("settings.items." + item.getTypeId()));
          event.setCancelled(true);
          return;
        }
        if (getConfig().contains("settings.items." + item.getTypeId() + ":" + item.getDurability()))
        {
          openInventory(player, getConfig().getString("settings.items." + item.getTypeId() + ":" + item.getDurability()));
          event.setCancelled(true);
          return;
        }
      }
    }
    if ((event.hasBlock()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK))
    {
      Block block = event.getClickedBlock();
      if ((block.getState() instanceof Sign))
      {
        Sign sign = (Sign)block.getState();
        if (sign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.signTitle"))))
        {
          event.setCancelled(true);
          openInventory(player, sign.getLine(2));
        }
      }
    }
  }
  
  @EventHandler
  public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
  {
    Player player = event.getPlayer();
    String[] args = event.getMessage().substring(1).toLowerCase().split(" ");
    String command = args[0];
    if (getConfig().contains("settings.commands." + command))
    {
      event.setCancelled(true);
      String inventory = getConfig().getString("settings.commands." + command);
      if (args.length == 1)
      {
        openInventory(player, inventory);
      }
      else if (player.hasPermission("compassnav.opensomeone"))
      {
        Player target = getServer().getPlayer(args[1]);
        if (target != null)
        {
          openInventory(target, inventory);
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.openedForMessage").replace("<player>", target.getName()).replace("<inventory>", inventory)));
        }
        else
        {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.notOnlineMessage").replace("<player>", args[1])));
        }
      }
      else
      {
        openInventory(player, inventory);
      }
    }
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event)
  {
    if ((event.getWhoClicked() instanceof Player))
    {
      Player player = (Player)event.getWhoClicked();
      if (this.inventories.containsKey(player.getName()))
      {
        event.setCancelled(true);
        String inventory = (String)this.inventories.get(player.getName());
        int slot = event.getRawSlot() + 1;
        if ((getConfig().contains("settings." + inventory + slot)) && (getConfig().getBoolean("settings." + inventory + slot + ".enabled"))) {
          if (player.hasPermission(new Permission("compassnav." + inventory + slot, PermissionDefault.TRUE)))
          {
            if (getConfig().getBoolean("settings.sounds")) {
              player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("settings.teleportSound").toUpperCase()), 1.0F, 1.0F);
            }
            checkUsage(player, inventory, slot);
          }
          else if (getConfig().getBoolean("settings.sounds"))
          {
            player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("settings.noPermSound").toUpperCase()), 1.0F, 1.0F);
          }
        }
      }
    }
  }
  
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
  {
    Player player = event.getPlayer();
    if ((this.timers.containsKey(player.getName())) && ((event.getTo().getX() != event.getFrom().getX()) || (event.getTo().getY() != event.getFrom().getY()) || (event.getTo().getZ() != event.getFrom().getZ())))
    {
      ((WarmupTimer)this.timers.get(player.getName())).cancel();
      this.timers.remove(player.getName());
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.teleportCancelMessage")));
    }
  }
  
  @EventHandler
  public void onSignChange(SignChangeEvent event)
  {
    Player player = event.getPlayer();
    if ((event.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.signTitle")))) || (event.getLine(0).equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.signTitle")))))) {
      if (player.hasPermission("compassnav.createsign")) {
        event.setLine(0, ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.signTitle")));
      } else {
        event.setLine(0, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.signTitle"))));
      }
    }
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    Player player = event.getPlayer();
    if (this.timers.containsKey(player.getName()))
    {
      ((WarmupTimer)this.timers.get(player.getName())).cancel();
      this.timers.remove(player.getName());
    }
    this.inventories.remove(player.getName());
  }
  
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event)
  {
    this.inventories.remove(event.getPlayer().getName());
  }
  
  public void sendHelpMessage(CommandSender sender)
  {
    for (String string : getConfig().getStringList("general.commandHelpMessage")) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
    }
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if ((cmd.getName().equalsIgnoreCase("cn")) || (cmd.getName().equalsIgnoreCase("compassnav"))) {
      if (args.length == 0) {
        sendHelpMessage(sender);
      } else if (args[0].equalsIgnoreCase("reload"))
      {
        if (sender.hasPermission("compassnav.reload"))
        {
          reloadConfig();
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.reloadMessage")));
        }
        else
        {
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.noPermMessage")));
        }
      }
      else if (args[0].equalsIgnoreCase("update"))
      {
        if (sender.hasPermission("compassnav.update"))
        {
          if (this.autoUpdater != null)
          {
            this.autoUpdater.download();
            if (this.autoUpdater.result == AutoUpdater.UpdateResult.SUCCESS) {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.updateSuccessMessage")));
            } else {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.updateFailedMessage")));
            }
          }
          else
          {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.cantUpdateMessage")));
          }
        }
        else {
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.noPermMessage")));
        }
      }
      else {
        sendHelpMessage(sender);
      }
    }
    return true;
  }
}
