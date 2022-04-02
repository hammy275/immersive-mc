package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ImmersiveChest extends AbstractTileEntityImmersive<ChestTileEntity, ChestInfo> {

    public static ImmersiveChest singleton = new ImmersiveChest();
    private final double spacing = 3d/16d;

    @Override
    public void tick(ChestInfo info, boolean isInVR) {
        super.tick(info, isInVR);
        if (!chestsValid(info)) return; // Return if we're waiting to remove this immersive

        // super.tick() does this for the main tileEntity. This does it for the other chest
        if (ThreadLocalRandom.current().nextInt(ClientConfig.inventorySyncTime) == 0) {
            Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.other.getBlockPos()));
        }
        ChestTileEntity[] chests = new ChestTileEntity[]{info.getTileEntity(), info.other};
        for (int i = 0; i <= 1; i++) {
            ChestTileEntity chest = chests[i];
            if (chest == null) continue;
            Direction forward = chest.getBlockState().getValue(HorizontalBlock.FACING);
            info.forward = forward;
            Vector3d pos = getTopCenterOfBlock(chest.getBlockPos());
            Direction left = getLeftOfDirection(forward);

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
            float hitboxSize = ClientConfig.itemScaleSizeChest / 3f;
            int startTop = 9 * info.getRowNum() + 27*i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.25, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.25, 0), hitboxSize));
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27*i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.5, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.5, 0), hitboxSize));
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27*i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.75, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.75, 0), hitboxSize));
            }

        }
    }

    @Override
    protected void render(ChestInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConfig.itemScaleSizeChest / info.getCountdown();
        Direction forward = info.forward;

        for (int i = 0; i < 27; i++) {
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i));
        }

        if (info.other != null) {
            for (int i = 27; i < 27 * 2; i++) {
                renderItem(info.items[i], stack, info.getPosition(i),
                        itemSize, forward, Direction.UP, info.getHibtox(i));
            }
        }
    }

    @Override
    public ChestInfo getNewInfo(ChestTileEntity tileEnt) {
        return new ChestInfo(tileEnt, ClientConfig.ticksToRenderChest, Util.getOther(tileEnt));
    }

    @Override
    public int getTickTime() {
        return ClientConfig.ticksToRenderChest;
    }

    @Override
    public boolean shouldRender(ChestInfo info, boolean isInVR) {
        boolean dataReady = info.forward != null && info.readyToRender();
        return !info.failRender && dataReady && chestsValid(info);
    }

    public boolean chestsValid(ChestInfo info) {
        boolean mainChestExists = info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.getBlockPosition()).getBlock() instanceof AbstractChestBlock;
        boolean otherChestExists = info.other == null ? true : (info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.other.getBlockPos()).getBlock() instanceof AbstractChestBlock);
        return mainChestExists && otherChestExists;
    }

    @Override
    public boolean shouldTrack(ChestTileEntity tileEnt) {
        // Make sure this isn't an "other" chest.
        ChestTileEntity other = Util.getOther(tileEnt);
        if (other != null) { // If we have an other chest, make sure that one isn't already being tracked
            for (ChestInfo info : ImmersiveChest.singleton.getTrackedObjects()) {
                if (info.getTileEntity() == other) { // If the info we're looking at is our neighboring chest
                    if (info.other == null) { // If our neighboring chest's info isn't tracking us
                        info.failRender = true;
                        info.other = tileEnt; // Track us
                        this.tick(info, VRPluginVerify.hasVR); // Tick so we can handle the items in our other chest
                        info.failRender = false;
                    }
                    return false; // Return false so this one isn't tracked
                }
            }
        }
        return super.shouldTrack(tileEnt);
    }

    public static ChestInfo findImmersive(ChestTileEntity chest) {
        Objects.requireNonNull(chest);
        for (ChestInfo info : singleton.getTrackedObjects()) {
            if (info.getTileEntity() == chest || info.other == chest) {
                return info;
            }
        }
        return null;
    }
}
