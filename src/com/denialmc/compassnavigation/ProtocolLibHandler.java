package com.denialmc.compassnavigation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.Packets;
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
    ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
    plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, 
    Packets.Server.SET_SLOT, Packets.Server.WINDOW_ITEMS) {
        @Override
        public void onPacketSending(PacketEvent event) { 
            if (event.getPacketID() == Packets.Server.SET_SLOT) {
                addGlow(new ItemStack[] { event.getPacket().getItemModifier().read(0) });
            } else {
                addGlow(event.getPacket().getItemArrayModifier().read(0));
            }
        }
    });
}
 
    private void addGlow(ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (stack != null) {
                // Only update those stacks that have our flag enchantment
                if (stack.getEnchantmentLevel(Enchantment.WATER_WORKER) == 4) {
                    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
                    compound.put(NbtFactory.ofList("ench"));
                }
            }
        }
    }
}
