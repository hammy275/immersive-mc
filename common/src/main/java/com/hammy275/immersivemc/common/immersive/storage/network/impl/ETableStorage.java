package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import net.minecraft.network.RegistryFriendlyByteBuf;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ETableStorage extends ListOfItemsStorage {
    public SlotData[] slots = new SlotData[]{SlotData.DEFAULT, SlotData.DEFAULT, SlotData.DEFAULT};
    public ApothStats apothStats = ApothStats.EMPTY;

    public ETableStorage(List<ItemStack> items) {
        super(items, 1);
    }

    public ETableStorage() {
        super();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        for (int i = 0; i < slots.length; i++) {
            slots[i].encode(buffer);
        }
        buffer.writeBoolean(apothStats != ApothStats.EMPTY);
        if (apothStats != ApothStats.EMPTY) {
            apothStats.encode(buffer);
        }
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = SlotData.decode(buffer);
        }
        if (buffer.readBoolean()) {
            apothStats = ApothStats.decode(buffer);
        }
    }

    public record SlotData(int xpLevel, List<Integer> enchantmentHints, List<Integer> enchantmentHintLevels) {

        public static final SlotData DEFAULT = new SlotData(0, List.of(), List.of());

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeInt(xpLevel);
            buffer.writeInt(enchantmentHints.size());
            enchantmentHints.forEach(buffer::writeInt);
            enchantmentHintLevels.forEach(buffer::writeInt);
        }

        public static SlotData decode(FriendlyByteBuf buffer) {
            int xpLevel = buffer.readInt();
            int numHints = buffer.readInt();
            List<Integer> hints = new ArrayList<>(numHints);
            for (int i = 0; i < numHints; i++) {
                hints.add(buffer.readInt());
            }
            List<Integer> hintLevels = new ArrayList<>(numHints);
            for (int i = 0; i < numHints; i++) {
                hintLevels.add(buffer.readInt());
            }
            return new SlotData(xpLevel, hints, hintLevels);
        }
    }
}
