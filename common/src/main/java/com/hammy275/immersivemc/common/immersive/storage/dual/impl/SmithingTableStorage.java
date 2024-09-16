package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.server.ServerUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class SmithingTableStorage extends ItemStorage {
    public SmithingTableStorage() {
        super(3, 1);
    }

    @Override
    public WorldStorageHandler<SmithingTableStorage> getHandler() {
        return ImmersiveHandlers.smithingTableHandler;
    }

    @Override
    public void load(CompoundTag nbt, int lastVanillaDataVersion) {
        // Fix bug in ImmersiveMC 1.5.0 Alpha 2 where we did conversions for 1.19->1.20 when it's still 1.19
        int numOfItems = nbt.getInt("numOfItems");
        if (numOfItems == 4) {
            ItemStack[] items = new ItemStack[4];
            for (int i = 0; i < items.length; i++) {
                items[i] = ServerUtil.parseItem(nbt.getCompound("item" + i), lastVanillaDataVersion);
            }
            if (items[0].isEmpty() && (!items[1].isEmpty() || !items[2].isEmpty())) {
                // At this point, it's extremely likely we suffered the bug from 1.5.0 Alpha 2.
                // The only exception is if a mod has added a recipe that only uses one item as input with that
                // item going in the right input slot, which I assume doesn't exist.
                nbt.put("item0", items[1].save(new CompoundTag()));
                nbt.put("item1", items[2].save(new CompoundTag()));
                // Don't have access to a player to get the smithing table output from, so this creates a tiny bug
                // where the output slot should have an output if the input recipe matches, when in actuality, we don't.
                // Not concerned about this, since it's a one-time thing from recovering from the mistake in
                // ImmersiveMC 1.5.0 Alpha 2.
                nbt.put("item2", ItemStack.EMPTY.save(new CompoundTag()));
            }
            nbt.remove("item3");
            nbt.putInt("numOfItems", 3);
        }
        super.load(nbt, lastVanillaDataVersion);
    }
}
