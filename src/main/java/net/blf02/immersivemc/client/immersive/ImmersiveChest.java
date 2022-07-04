package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ChestOpenPacket;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.vr.util.Util;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Objects;

public class ImmersiveChest extends AbstractTileEntityImmersive<TileEntity, ChestInfo> {

    public static ImmersiveChest singleton = new ImmersiveChest();
    private final double spacing = 3d/16d;
    private final double threshold = 0.03;

    public ImmersiveChest() {
        super(4);
    }

    @Override
    protected void doTick(ChestInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

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
            int startTop = 9 * info.getRowNum() + 27 * i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.2, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.2, 0), hitboxSize));
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27 * i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.325, 0));
                info.setHitbox(z, null);
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27 * i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vector3d posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.45, 0));
                info.setHitbox(z, null);
            }
        }

        for (int chestNum = 0; chestNum <= 1; chestNum++) {
            TileEntity chest = chests[chestNum];
            if (chest == null) continue;
            Vector3d forward = Vector3d.atLowerCornerOf(info.forward.getNormal());
            Vector3d left = Vector3d.atLowerCornerOf(getLeftOfDirection(info.forward).getNormal());
            Vector3d frontMid = getTopCenterOfBlock(chest.getBlockPos()).add(forward.multiply(0.5, 0.5, 0.5));
            if (info.isOpen) {
                Vector3d linePos = frontMid.add(forward.multiply(-0.5, -0.5, -0.5));
                linePos = linePos.add(0, 0.5, 0);
                info.openClosePositions[chestNum] = linePos;
                info.openCloseHitboxes[chestNum] = new AxisAlignedBB(
                        linePos.add(left.multiply(-0.5, -0.5, -0.5)).add(0, -1d/4d, 0)
                          .add(forward.multiply(-0.625, -0.625, -0.625)),
                        linePos.add(left.multiply(0.5, 0.5, 0.5)).add(0, 1d/4d, 0)
                                .add(forward.multiply(0.625, 0.625, 0.625))
                );
            } else {
                Vector3d linePos = frontMid.add(0, -0.375, 0);
                info.openClosePositions[chestNum] = linePos;
                info.openCloseHitboxes[chestNum] = new AxisAlignedBB(
                        linePos.add(left.multiply(-0.5, -0.5, -0.5)).add(0, -1d/4d, 0)
                                .add(forward.multiply(-0.15, -0.15, -0.15)),
                        linePos.add(left.multiply(0.5, 0.5, 0.5)).add(0, 1d/4d, 0)
                                .add(forward.multiply(0.15, 0.15, 0.15))
                );
            }
        }

        if (info.openCloseCooldown <= 0 && !ActiveConfig.rightClickChest) {
            if (VRPluginVerify.clientInVR && VRPlugin.API.apiActive(Minecraft.getInstance().player)
                    && info.openCloseHitboxes != null) {
                Vector3d current0 = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController0().position();
                Vector3d current1 = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController1().position();

                double diff0 = current0.y - info.lastY0;
                double diff1 = current1.y - info.lastY1;
                if (!Util.getFirstIntersect(current0, info.openCloseHitboxes).isPresent()) {
                    diff0 = 0;
                }
                if (!Util.getFirstIntersect(current1, info.openCloseHitboxes).isPresent()) {
                    diff1 = 0;
                }

                boolean cond;
                if (info.isOpen) {
                    cond = diff0 <= -threshold || diff1 <= -threshold;
                } else {
                    cond = diff0 >= threshold || diff1 >= threshold;
                }

                if (cond) {
                    openChest(info);
                    info.openCloseCooldown = 40;
                }

                info.lastY0 = current0.y;
                info.lastY1 = current1.y;
            }
        } else if (!ActiveConfig.rightClickChest) {
            info.openCloseCooldown--;
        }
    }

    @Override
    protected void render(ChestInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeChest / info.getItemTransitionCountdown();
        Direction forward = info.forward;

        if (info.isOpen) {
            for (int i = 0; i < 27; i++) {
                int startTop = 9 * info.getRowNum();
                int endTop = startTop + 9;
                boolean showCount = i >= startTop && i <= endTop;
                renderItem(info.items[i], stack, info.getPosition(i),
                        itemSize, forward, Direction.UP, info.getHitbox(i), showCount);
            }

            if (info.other != null) {
                for (int i = 27; i < 27 * 2; i++) {
                    int startTop = 9 * info.getRowNum() + 27;
                    int endTop = startTop + 9 + 27;
                    boolean showCount = i >= startTop && i <= endTop;
                    renderItem(info.items[i], stack, info.getPosition(i),
                            itemSize, forward, Direction.UP, info.getHitbox(i), showCount);
                }
            }
        }

        for (int i = 0; i <= 1; i++) {
            if (info.openCloseHitboxes[i] != null && info.openClosePositions[i] != null) {
                renderHitbox(stack, info.openCloseHitboxes[i], info.openClosePositions[i]);
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
    public boolean hasValidBlock(ChestInfo info, World level) {
        return chestsValid(info);
    }

    @Override
    public boolean shouldRender(ChestInfo info, boolean isInVR) {
        boolean dataReady = info.forward != null && info.readyToRender();
        return !info.failRender && dataReady && chestsValid(info);
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
        if (!VRPluginVerify.clientInVR && !ActiveConfig.rightClickChest) return;
        if (!((ChestInfo) info).isOpen) return;
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

    @Override
    public void onRemove(ChestInfo info) {
        super.onRemove(info);
        if (info.isOpen) {
            openChest(info);
        }
    }

    @Override
    protected void initInfo(ChestInfo info) {
        // NOOP since a chest in a double chest can be broken at any time
    }

    public static void openChest(ChestInfo info) {
        info.isOpen = !info.isOpen;
        Network.INSTANCE.sendToServer(new ChestOpenPacket(info.getBlockPosition(), info.isOpen));
        if (!info.isOpen) {
            info.remove(); // Remove immersive if we're closing the chest
        }
    }



}
