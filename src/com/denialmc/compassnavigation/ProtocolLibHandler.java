package com.denialmc.compassnavigation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ProtocolLibHandler
{
  public ProtocolLibHandler(CompassNavigation plugin)
  {
    ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, new Integer[] { Integer.valueOf(103), Integer.valueOf(104) })
    {
      public void onPacketSending(PacketEvent event)
      {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
          ProtocolLibHandler.this.modifyItem(new ItemStack[] { (ItemStack)event.getPacket().getItemModifier().read(0) });
        } else {
          ProtocolLibHandler.this.modifyItem((ItemStack[])event.getPacket().getItemArrayModifier().read(0));
        }
      }
    });
  }
  
  public void modifyItem(ItemStack[] stacks)
  {
    for (ItemStack stack : stacks) {
      if (stack != null)
      {
        NbtCompound compound = (NbtCompound)NbtFactory.fromItemTag(stack);
        compound.put(NbtFactory.ofList("AttributeModifiers", new Object[0]));
        if (stack.getEnchantmentLevel(Enchantment.WATER_WORKER) == 4) {
          compound.put(NbtFactory.ofList("ench", new Object[0]));
        }
      }
    }
  }
}
