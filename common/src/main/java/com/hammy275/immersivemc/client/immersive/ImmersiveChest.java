package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.client.VRClientAPI;

import java.util.List;
import java.util.Objects;

public class ImmersiveChest extends AbstractImmersive<ChestInfo, ListOfItemsStorage> {
    public static final double spacing = 3d/16d;
    private final double threshold = 0.03;
    // Intentionally stored outside infos, so a chest close (which removes the info) will still have a cooldown
    // before you can open a chest again.
    public int openCloseCooldown = 0;

    @Override
    public void globalTick() {
        super.globalTick();
        if (openCloseCooldown > 0) {
            openCloseCooldown--;
        }
        this.infos.removeIf((info) -> !chestsValid(info));
    }

    @Override
    public ChestInfo buildInfo(BlockPos pos, Level level) {
        BlockEntity blockEnt = level.getBlockEntity(pos);
        if (blockEnt instanceof ChestBlockEntity) {
            return new ChestInfo(blockEnt, Util.getOtherChest((ChestBlockEntity) blockEnt));
        } else if (blockEnt instanceof EnderChestBlockEntity) {
            return new ChestInfo(blockEnt, null);
        }
        throw new IllegalArgumentException("ImmersiveChest can only track chests and ender chests!");
    }

    @Override
    public int handleHitboxInteract(ChestInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        if (!info.isOpen) return -1;
        ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), hitboxIndices, hand, false);
        return ImmersiveClientConstants.instance().defaultCooldown();
    }

    @Override
    public boolean shouldRender(ChestInfo info) {
        return info.hasHitboxes();
    }

    @Override
    public void render(ChestInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {

        if (info.isOpen) {
            for (int i = 0; i < 27; i++) {
                int startTop = 9 * info.getRowNum();
                int endTop = startTop + 9;
                boolean showCount = i >= startTop && i <= endTop;
                helpers.renderItemWithInfo(info.hitboxes.get(i).item, stack, ClientConstants.itemScaleSizeChest,
                        showCount, info.light, info, true, i, null, info.forward, Direction.UP);
            }

            if (info.otherChest != null) {
                for (int i = 27; i < 27 * 2; i++) {
                    int startTop = 9 * info.getRowNum() + 27;
                    int endTop = startTop + 9 + 27;
                    boolean showCount = i >= startTop && i <= endTop;
                    helpers.renderItemWithInfo(info.hitboxes.get(i).item, stack, ClientConstants.itemScaleSizeChest,
                            showCount, info.light, info, true, i, null, info.forward, Direction.UP);
                }
            }
        }

        for (int i = 0; i <= 1; i++) {
            if (info.openCloseHitboxes[i] != null && info.openClosePositions[i] != null) {
                helpers.renderHitbox(stack, info.openCloseHitboxes[i]);
            }
        }
    }

    @Override
    public void tick(ChestInfo info) {
        super.tick(info);
        info.light = ImmersiveClientLogicHelpers.instance().getLight(info.getBlockPosition().above());
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof ChestBlockEntity cbe) {
            info.otherChest = Util.getOtherChest(cbe);
            if (info.otherChest != null) {
                info.otherPos = info.otherChest.getBlockPos();
            }
        }

        BlockEntity[] chests = new BlockEntity[]{info.chest, info.otherChest};
        for (int i = 0; i <= 1; i++) {
            BlockEntity chest = chests[i];
            if (chest == null) continue;
            info.forward = chest.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

            Vec3[] positions = Util.get3x3HorizontalGrid(chest.getBlockPos(), spacing, info.forward,
                    false);
            float hitboxSize = ClientConstants.itemScaleSizeChest / 3f * 2.2f;
            int startTop = 9 * info.getRowNum() + 27 * i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startTop) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.2 + offset, offset),
                        hitboxSize, hitboxSize, hitboxSize);
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27 * i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startMid) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.325 + offset, offset),
                        0, 0, 0);
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27 * i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startBot) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.45 + offset, offset),
                        0, 0, 0);
            }
        }

        for (int chestNum = 0; chestNum <= 1; chestNum++) {
            BlockEntity chest = chests[chestNum];
            if (chest == null) continue;
            Vec3 forward = info.forward.getUnitVec3();
            Vec3 left = info.forward.getCounterClockWise().getUnitVec3();
            Vec3 frontMid = Vec3.upFromBottomCenterOf(chest.getBlockPos(), 1).add(forward.multiply(0.5, 0.5, 0.5));
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

        if (openCloseCooldown <= 0 && !ActiveConfig.active().rightClickChestInteractions) {
            if (VRVerify.playerInVR(Minecraft.getInstance().player) && info.openCloseHitboxes != null) {
                Vec3 current0 = VRClientAPI.instance().getPreTickWorldPose().getMainHand().getPos();
                Vec3 current1 = VRClientAPI.instance().getPreTickWorldPose().getOffHand().getPos();

                double diff0 = current0.y - info.lastY0;
                double diff1 = current1.y - info.lastY1;
                if (Util.getFirstIntersect(current0, info.openCloseHitboxes).isEmpty()) {
                    diff0 = 0;
                }
                if (Util.getFirstIntersect(current1, info.openCloseHitboxes).isEmpty()) {
                    diff1 = 0;
                }

                boolean cond;
                if (info.isOpen) {
                    cond = diff0 <= -threshold || diff1 <= -threshold;
                } else {
                    cond = diff0 >= threshold || diff1 >= threshold;
                }

                if (cond) {
                    if (!info.isOpen) {
                        // Use a distance check for checking if to vibrate the other controller to hopefully filter out
                        // actions of moving up that are for something other than the chest
                        if (diff0 >= threshold) {
                            VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimeWorldInteraction);
                            if (diff1 >= threshold / 5d && current0.distanceToSqr(current1) <= 1) {
                                VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.OFF_HAND, CommonConstants.vibrationTimeWorldInteraction);
                            }
                        }
                        if (diff1 >= threshold) {
                            VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.OFF_HAND, CommonConstants.vibrationTimeWorldInteraction);
                            if ((diff0 >= threshold / 5d && current0.distanceToSqr(current1) <= 1)) {
                                VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimeWorldInteraction);
                            }
                        }
                    }
                    openChest(info);
                    openCloseCooldown = 40;
                }

                info.lastY0 = current0.y;
                info.lastY1 = current1.y;
            }
        }
    }

    @Override
    @Nullable
    public AABB getDragHitbox(ChestInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(ChestInfo info, int hitboxIndex) {
        return true;
    }

    @Override
    public ImmersiveHandler<ListOfItemsStorage> getHandler() {
        return ImmersiveHandlers.chestHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("chest", () -> new ItemStack(Items.CHEST),
                config -> config.useChestImmersive,
                (config, newVal) -> config.useChestImmersive = newVal);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(ChestInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(ChestInfo info, ListOfItemsStorage storage) {
        for (int i = 0; i < storage.getItems().size(); i++) {
            info.hitboxes.get(i).item = storage.getItems().get(i);
        }
    }

    @Override
    public boolean isVROnly() {
        return false;
    }

    public boolean chestsValid(ChestInfo info) {
        boolean mainChestExists = getHandler().isValidBlock(info.getBlockPosition(), info.chest.getLevel());
        boolean otherChestExists = info.otherChest == null ||
                (info.chest.getLevel() != null && info.chest.getLevel().getBlockEntity(info.otherPos) instanceof ChestBlockEntity);
        return mainChestExists && otherChestExists;
    }

    public static ChestInfo findImmersive(BlockEntity chest) {
        Objects.requireNonNull(chest);
        for (ChestInfo info : Immersives.immersiveChest.getTrackedObjects()) {
            if (info.chest == chest || info.otherChest == chest) {
                return info;
            }
        }
        return null;
    }

    public static void openChest(ChestInfo info) {
        info.isOpen = !info.isOpen;
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(info.getBlockPosition(), info.isOpen));
        if (info.isOpen) {
            Lootr.lootrImpl.markOpener(Minecraft.getInstance().player, info.getBlockPosition());
        }
    }
}
