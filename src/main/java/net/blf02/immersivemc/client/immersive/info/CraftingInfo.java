package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class CraftingInfo extends AbstractImmersiveInfo {

    // Output is from left clicking the crafting table with a valid recipe, so no need
    // for an actual slot

    public final BlockPos tablePos;

    protected AxisAlignedBB[] inputs = new AxisAlignedBB[]{
            null, null, null,
            null, null, null,
            null, null, null
    };

    protected Vector3d[] positions = new Vector3d[9];

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

    @Override
    public Vector3d getPosition(int slot) {
        return positions[slot];
    }

    @Override
    public Vector3d[] getAllPositions() {
        return positions;
    }

    @Override
    public void setPosition(int slot, Vector3d position) {
        positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return positions[0] != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes();
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.tablePos;
    }
}
