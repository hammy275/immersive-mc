package com.hammy275.immersivemc.common.immersive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ETableStorage extends ListOfItemsStorage {
    public int[] xpLevels = new int[]{-1, -1, -1};
    public int[] enchantHints = new int[]{-1, -1, -1};
    public int[] levelHints = new int[]{-1, -1, -1};

    public ETableStorage(List<ItemStack> items) {
        super(items, 1);
    }

    public ETableStorage() {
        super();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        for (int xpLevel : xpLevels) {
            buffer.writeInt(xpLevel);
        }
        for (int enchantHint : enchantHints) {
            buffer.writeInt(enchantHint);
        }
        for (int levelHint : levelHints) {
            buffer.writeInt(levelHint);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.xpLevels = new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()};
        this.enchantHints = new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()};
        this.levelHints = new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()};
    }
}
