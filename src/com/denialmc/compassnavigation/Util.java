/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.denialmc.compassnavigation;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Mario
 */
public class Util {
    
    public static ItemStack removeAttributes(ItemStack item) {
        if (!MinecraftReflection.isCraftItemStack(item)) {
            item = MinecraftReflection.getBukkitItemStack(item);
        }
        NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
        compound.put(NbtFactory.ofList("AttributeModifiers"));
        return item;
    }
    
    public static ItemStack addGlow(ItemStack item) {
        NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
	compound.put(NbtFactory.ofList("ench"));
        return item;
    }
    
}
