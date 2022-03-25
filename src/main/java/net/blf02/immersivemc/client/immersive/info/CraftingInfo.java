package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CraftingInfo extends AbstractImmersiveInfo {

    // Output is from left clicking the crafting table with a valid recipe, so no need
    // for an actual slot

    public final BlockPos tablePos;

    protected AxisAlignedBB[] inputs = new AxisAlignedBB[]{
            null, null, null,
            null, null, null,
            null, null, null
    };

    public CraftingInfo(BlockPos pos, int ticksToExist) {
        super(ticksToExist);
        this.tablePos = pos;
    }

    @Override
    public AxisAlignedBB getHibtox(int slot) {
        return inputs[slot];
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return inputs;
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        inputs[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return inputs[0] != null;
    }
}
