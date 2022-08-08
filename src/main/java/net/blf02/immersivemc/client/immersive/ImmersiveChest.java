package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ChestOpenPacket;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveChest extends AbstractBlockEntityImmersive<BlockEntity, ChestInfo> {

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
        // (which don't implement Container)
        if (info.ticksActive % ClientConstants.inventorySyncTime == 0) {
            if (info.other != null) {
                Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.other.getBlockPos()));
            } else if (info.getBlockEntity() instanceof EnderChestBlockEntity) {
                Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
            }
        }

        BlockEntity[] chests = new BlockEntity[]{info.getBlockEntity(), info.other};
        for (int i = 0; i <= 1; i++) {
            BlockEntity chest = chests[i];
            if (chest == null) continue;
            Direction forward = chest.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            info.forward = forward;
            Vec3 pos = getTopCenterOfBlock(chest.getBlockPos());
            Direction left = getLeftOfDirection(forward);

            Vec3 leftOffset = new Vec3(
                    left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);
            Vec3 rightOffset = new Vec3(
                    left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);

            Vec3 topOffset = new Vec3(
                    forward.getNormal().getX() * -spacing, 0, forward.getNormal().getZ() * -spacing);
            Vec3 botOffset = new Vec3(
                    forward.getNormal().getX() * spacing, 0, forward.getNormal().getZ() * spacing);


            Vec3[] positions = new Vec3[]{
                    pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                    pos.add(leftOffset), pos, pos.add(rightOffset),
                    pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
            };
            float hitboxSize = ClientConstants.itemScaleSizeChest / 3f * 1.1f;
            int startTop = 9 * info.getRowNum() + 27 * i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vec3 posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.2, 0));
                info.setHitbox(z, createHitbox(posRaw.add(0, -0.2, 0), hitboxSize));
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27 * i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vec3 posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.325, 0));
                info.setHitbox(z, null);
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27 * i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vec3 posRaw = positions[z % 9];
                info.setPosition(z, posRaw.add(0, -0.45, 0));
                info.setHitbox(z, null);
            }
        }

        for (int chestNum = 0; chestNum <= 1; chestNum++) {
            BlockEntity chest = chests[chestNum];
            if (chest == null) continue;
            Vec3 forward = Vec3.atLowerCornerOf(info.forward.getNormal());
            Vec3 left = Vec3.atLowerCornerOf(getLeftOfDirection(info.forward).getNormal());
            Vec3 frontMid = getTopCenterOfBlock(chest.getBlockPos()).add(forward.multiply(0.5, 0.5, 0.5));
            if (info.isOpen) {
                Vec3 linePos = frontMid.add(forward.multiply(-0.5, -0.5, -0.5));
                linePos = linePos.add(0, 0.5, 0);
                info.openClosePositions[chestNum] = linePos;
                info.openCloseHitboxes[chestNum] = new AABB(
                        linePos.add(left.multiply(-0.5, -0.5, -0.5)).add(0, -1d/4d, 0)
                          .add(forward.multiply(-0.625, -0.625, -0.625)),
                        linePos.add(left.multiply(0.5, 0.5, 0.5)).add(0, 1d/4d, 0)
                                .add(forward.multiply(0.625, 0.625, 0.625))
                );
            } else {
                Vec3 linePos = frontMid.add(0, -0.375, 0);
                info.openClosePositions[chestNum] = linePos;
                info.openCloseHitboxes[chestNum] = new AABB(
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
                Vec3 current0 = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController0().position();
                Vec3 current1 = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController1().position();

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
    protected boolean slotShouldRenderHelpHitbox(ChestInfo info, int slotNum) {
        if (info.getBlockEntity() instanceof EnderChestBlockEntity) {
            return info.items[slotNum] == null || info.items[slotNum].isEmpty();
        }
        return super.slotShouldRenderHelpHitbox(info, slotNum);
    }

    @Override
    protected void render(ChestInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeChest / info.getItemTransitionCountdown();
        Direction forward = info.forward;

        if (info.isOpen) {
            for (int i = 0; i < 27; i++) {
                int startTop = 9 * info.getRowNum();
                int endTop = startTop + 9;
                boolean showCount = i >= startTop && i <= endTop;
                renderItem(info.items[i], stack, info.getPosition(i),
                        itemSize, forward, Direction.UP, info.getHitbox(i), showCount, -1);
            }

            if (info.other != null) {
                for (int i = 27; i < 27 * 2; i++) {
                    int startTop = 9 * info.getRowNum() + 27;
                    int endTop = startTop + 9 + 27;
                    boolean showCount = i >= startTop && i <= endTop;
                    renderItem(info.items[i], stack, info.getPosition(i),
                            itemSize, forward, Direction.UP, info.getHitbox(i), showCount, -1);
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
    public ChestInfo getNewInfo(BlockEntity tileEnt) {
        if (tileEnt instanceof ChestBlockEntity) {
            return new ChestInfo(tileEnt, ClientConstants.ticksToRenderChest, Util.getOtherChest((ChestBlockEntity) tileEnt));
        } else if (tileEnt instanceof EnderChestBlockEntity) {
            return new ChestInfo(tileEnt, ClientConstants.ticksToRenderChest, null);
        }
        throw new IllegalArgumentException("ImmersiveChest can only track chests and ender chests!");
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderChest;
    }

    @Override
    public boolean hasValidBlock(ChestInfo info, Level level) {
        return chestsValid(info);
    }

    @Override
    public boolean shouldRender(ChestInfo info, boolean isInVR) {
        boolean dataReady = info.forward != null && info.readyToRender();
        return !info.failRender && dataReady && chestsValid(info);
    }

    public boolean chestsValid(ChestInfo info) {
        try {
            Block mainChestBlock = info.getBlockEntity().getLevel().getBlockState(info.getBlockPosition()).getBlock();
            boolean mainChestExists = mainChestBlock instanceof AbstractChestBlock || mainChestBlock instanceof EnderChestBlock;
            boolean otherChestExists = info.other == null ? true : (info.getBlockEntity().getLevel() != null &&
                    info.getBlockEntity().getLevel().getBlockState(info.other.getBlockPos()).getBlock() instanceof AbstractChestBlock);
            return mainChestExists && otherChestExists;
        } catch (NullPointerException e) {
            return false;
        }

    }

    @Override
    public boolean shouldTrack(BlockEntity tileEnt) {
        // Make sure this isn't an "other" chest.
        if (tileEnt instanceof ChestBlockEntity) {
            ChestBlockEntity other = Util.getOtherChest((ChestBlockEntity) tileEnt);
            if (other != null) { // If we have an other chest, make sure that one isn't already being tracked
                for (ChestInfo info : ImmersiveChest.singleton.getTrackedObjects()) {
                    if (info.getBlockEntity() == other) { // If the info we're looking at is our neighboring chest
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
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        if (!VRPluginVerify.clientInVR && !ActiveConfig.rightClickChest) return;
        if (!((ChestInfo) info).isOpen) return;
        Network.INSTANCE.sendToServer(new SwapPacket(
                info.getBlockPosition(), closest, hand
        ));
    }

    public static ChestInfo findImmersive(BlockEntity chest) {
        Objects.requireNonNull(chest);
        for (ChestInfo info : singleton.getTrackedObjects()) {
            if (info.getBlockEntity() == chest || info.other == chest) {
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
