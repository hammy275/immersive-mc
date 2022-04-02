package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveChest extends AbstractTileEntityImmersive<ChestTileEntity, ChestInfo> {

    public static ImmersiveChest singleton = new ImmersiveChest();
    private final double spacing = 3d/16d;

    @Override
    public void tick(ChestInfo info, boolean isInVR) {
        super.tick(info, isInVR);
        ChestTileEntity[] chests = new ChestTileEntity[]{info.getTileEntity(), info.other};
        for (int i = 0; i <= 1; i++) {
            ChestTileEntity chest = chests[i];
            Direction forward = chest.getBlockState().getValue(HorizontalBlock.FACING);
            info.forward = forward;
            Vector3d pos = getTopCenterOfBlock(chest.getBlockPos());
            Direction left = getLeftOfDirection(forward);


            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                info.items[27 * i + slot] = chest.getItem(slot);
            }

            Vector3d leftOffset = new Vector3d(
                    left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);
            Vector3d rightOffset = new Vector3d(
                    left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);

            Vector3d topOffset = new Vector3d(
                    forward.getNormal().getX() * -spacing, 0, forward.getNormal().getZ() * -spacing);
            Vector3d botOffset = new Vector3d(
                    forward.getNormal().getX() * spacing, 0, forward.getNormal().getZ() * spacing);


            Vector3d[] positions = new Vector3d[]{
                    pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                    pos.add(leftOffset), pos, pos.add(rightOffset),
                    pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
            };
            float hitboxSize = ClientConfig.itemScaleSizeCrafting / 3f;
            for (int z = 18 + 27*i; z < 27 + 27*i; z++) {
                info.setPosition(z, positions[z - 18 - 27*i]);
                info.setHitbox(z, createHitbox(positions[z - 18 - 27*i], hitboxSize));
            }

        }
    }

    @Override
    protected void render(ChestInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConfig.itemScaleSizeCrafting / info.getCountdown();
        Direction forward = info.forward;

        for (int i = 18; i < 27; i++) {
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i));
        }

        for (int i = 18 + 27; i < 27*2; i++) {
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i));
        }
    }

    @Override
    public ChestInfo getNewInfo(ChestTileEntity tileEnt) {

        return new ChestInfo(tileEnt, ClientConfig.ticksToRenderChest, getOther(tileEnt));
    }

    @Override
    public int getTickTime() {
        return ClientConfig.ticksToRenderChest;
    }

    @Override
    public boolean shouldRender(ChestInfo info, boolean isInVR) {
        return info.forward != null && info.readyToRender() ; // TODO: Add VR only check
    }

    @Override
    public boolean shouldTrack(ChestTileEntity tileEnt) {
        // Make sure this isn't an "other" chest.
        ChestTileEntity other = getOther(tileEnt);
        if (other != null) { // If we have an other chest, make sure that one isn't already being tracked
            for (ChestInfo info : ImmersiveChest.singleton.getTrackedObjects()) {
                if (info.getTileEntity() == other) { // If the info we're looking at is our neighboring chest
                    if (info.other == null) { // If our neighboring chest's info isn't tracking us
                        info.other = tileEnt; // Track us
                    }
                    return false; // Return false so this one isn't tracked
                }
            }
        }
        return super.shouldTrack(tileEnt);
    }

    public ChestTileEntity getOther(ChestTileEntity chest) {
        // Gets the chest this one is connected to. Can be null.
        Direction otherDir = ChestBlock.getConnectedDirection(chest.getBlockState());
        BlockPos otherPos = chest.getBlockPos().relative(otherDir);
        if (chest.getLevel() != null && chest.getLevel().getBlockEntity(otherPos) instanceof ChestTileEntity) {
            return (ChestTileEntity) chest.getLevel().getBlockEntity(otherPos);
        }
        return null;
    }
}
