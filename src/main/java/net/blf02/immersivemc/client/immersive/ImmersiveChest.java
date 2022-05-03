package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

public class ImmersiveChest extends AbstractTileEntityImmersive<TileEntity, ChestInfo> {

    public static ImmersiveChest singleton = new ImmersiveChest();
    private final double spacing = 3d/16d;

    public ImmersiveChest() {
        super(4);
    }

    @Override
    protected void doTick(ChestInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (!chestsValid(info)) return; // Return if we're waiting to remove this immersive

        // super.tick() does this for the main regular chest. This does it for the other chest, and for ender chests
        // (which don't implement IInventory)
        if (info.ticksActive % ClientConstants.inventorySyncTime == 0) {
            if (info.other != null) {
                Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.other.getBlockPos()));
            } else if (info.getTileEntity() instanceof EnderChestTileEntity) {
                Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
            }
        }

        TileEntity[] chests = new TileEntity[]{info.getTileEntity(), info.other};
        for (int i = 0; i <= 1; i++) {
            TileEntity chest = chests[i];
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
            float hitboxSize = ClientConstants.itemScaleSizeChest / 3f * 1.1f;
            int startTop = 9 * info.getRowNum() + 27*i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.2, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.2, 0), hitboxSize));
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27*i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.325, 0));
                info.setHitbox(z, null);
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27*i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.45, 0));
                info.setHitbox(z, null);
            }

        }
    }

    @Override
    protected void render(ChestInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeChest / info.getCountdown();
        Direction forward = info.forward;

        for (int i = 0; i < 27; i++) {
            int startTop = 9 * info.getRowNum();
            int endTop = startTop + 9;
            boolean showCount = i >= startTop && i <= endTop;
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i), showCount);
        }

        if (info.other != null) {
            for (int i = 27; i < 27 * 2; i++) {
                int startTop = 9 * info.getRowNum() + 27;
                int endTop = startTop + 9 + 27;
                boolean showCount = i >= startTop && i <= endTop;
                renderItem(info.items[i], stack, info.getPosition(i),
                        itemSize, forward, Direction.UP, info.getHibtox(i), showCount);
            }
        }
    }

    @Override
    public ChestInfo getNewInfo(TileEntity tileEnt) {
        if (tileEnt instanceof ChestTileEntity) {
            return new ChestInfo(tileEnt, ClientConstants.ticksToRenderChest, Util.getOtherChest((ChestTileEntity) tileEnt));
        } else if (tileEnt instanceof EnderChestTileEntity) {
            return new ChestInfo(tileEnt, ClientConstants.ticksToRenderChest, null);
        }
        throw new IllegalArgumentException("ImmersiveChest can only track chests and ender chests!");
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderChest;
    }

    @Override
    public boolean shouldRender(ChestInfo info, boolean isInVR) {
        boolean dataReady = info.forward != null && info.readyToRender();
        return !info.failRender && dataReady && chestsValid(info) && info.isOpen;
    }

    public boolean chestsValid(ChestInfo info) {
        try {
            Block mainChestBlock = info.getTileEntity().getLevel().getBlockState(info.getBlockPosition()).getBlock();
            boolean mainChestExists = mainChestBlock instanceof AbstractChestBlock || mainChestBlock instanceof EnderChestBlock;
            boolean otherChestExists = info.other == null ? true : (info.getTileEntity().getLevel() != null &&
                    info.getTileEntity().getLevel().getBlockState(info.other.getBlockPos()).getBlock() instanceof AbstractChestBlock);
            return mainChestExists && otherChestExists;
        } catch (NullPointerException e) {
            return false;
        }

    }

    @Override
    public boolean shouldTrack(TileEntity tileEnt) {
        // Make sure this isn't an "other" chest.
        if (tileEnt instanceof ChestTileEntity) {
            ChestTileEntity other = Util.getOtherChest((ChestTileEntity) tileEnt);
            if (other != null) { // If we have an other chest, make sure that one isn't already being tracked
                for (ChestInfo info : ImmersiveChest.singleton.getTrackedObjects()) {
                    if (info.getTileEntity() == other) { // If the info we're looking at is our neighboring chest
                        if (info.other == null) { // If our neighboring chest's info isn't tracking us
                            info.failRender = true;
                            info.other = tileEnt; // Track us
                            this.doTick(info, VRPluginVerify.clientInVR); // Tick so we can handle the items in our other chest
                            info.failRender = false;
                        }
                        return false; // Return false so this one isn't tracked
                    }
                }
            }
        }
        return super.shouldTrack(tileEnt);
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useChestImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        Network.INSTANCE.sendToServer(new SwapPacket(
                info.getBlockPosition(), closest, hand
        ));
    }

    public static ChestInfo findImmersive(TileEntity chest) {
        Objects.requireNonNull(chest);
        for (ChestInfo info : singleton.getTrackedObjects()) {
            if (info.getTileEntity() == chest || info.other == chest) {
                return info;
            }
        }
        return null;
    }


}
