package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BuiltImmersive extends AbstractImmersive<BuiltImmersiveInfo> {

    protected final ImmersiveBuilder builder;

    public BuiltImmersive(ImmersiveBuilder builder) {
        super(builder.maxImmersives);
        this.builder = builder;
        if (builder.usesWorldStorage) {
            Immersives.WS_IMMERSIVES.add(this);
        }
    }

    @Override
    public boolean isVROnly() {
        return builder.vrOnly;
    }

    @Override
    public boolean shouldRender(BuiltImmersiveInfo info, boolean isInVR) {
        return
                shouldTrack(info.getBlockPosition()) && // Check that block is still there
                        info.readyToRender() &&
                        airCheck(info) &&
                        builder.extraRenderReady.apply(info);
    }

    @Override
    protected void doTick(BuiltImmersiveInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        if (!info.itemHitboxes.isEmpty() && info.ticksActive % ClientConstants.inventorySyncTime == 0) {
            Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
        }
        Direction currentDir;
        switch (builder.positioningMode) {
            case HORIZONTAL_BLOCK_FACING, BLOCK_FACING_NEG_X ->
                    currentDir = info.immersiveDir;
            case TOP_PLAYER_FACING, TOP_BLOCK_FACING, HORIZONTAL_PLAYER_FACING, PLAYER_FACING_NO_DOWN ->
                    currentDir = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
            case TOP_LITERAL ->
                    currentDir = null;
            case PLAYER_FACING_FILTER_BLOCK_FACING ->
                    currentDir = getForwardFromPlayerUpAndDownFilterBlockFacing(Minecraft.getInstance().player, info.getBlockPosition(), true);
            default ->
                    throw new UnsupportedOperationException("Facing direction for positioning mode " + builder.positioningMode + " unimplemented!");

        }

        for (int i = 0; i < info.hitboxes.length; i++) {
            HitboxInfo hitbox = info.hitboxes[i];
            // Update hitbox if its offset isn't constant, the current direction isn't the same as the last,
            // if it hasn't been calculated yet, or if slots can change whether they're active.
            if (!hitbox.constantOffset || currentDir != info.immersiveDir || !hitbox.calcDone() || builder.slotActive != ImmersiveBuilder.SLOT_ALWAYS_ACTIVE) {
                if (builder.slotActive.apply(info, i)) {
                    hitbox.recalculate(Minecraft.getInstance().level, builder.positioningMode, info);
                } else {
                    // Force hitbox to be null if not active. Prevents rendering and hitbox collision, but still keeps
                    // the slot available to receive items from the network, etc.
                    hitbox.forceNull();
                }
            }

        }

        info.immersiveDir = currentDir;
    }

    @Override
    protected void render(BuiltImmersiveInfo info, PoseStack stack, boolean isInVR) {
        float size = builder.renderSize / info.getItemTransitionCountdown();
        for (int i = 0; i < info.hitboxes.length; i++) {
            HitboxInfo hitbox = info.hitboxes[i];
            if (hitbox.holdsItems) {
                int spinDegrees = hitbox.itemSpins ? (int) (info.ticksActive % 100d * 3.6d) : -1;
                renderItem(hitbox.item, stack, hitbox.getPos(),
                        info.slotHovered == i ? size * 1.25f * hitbox.itemRenderSizeMultiplier : size * hitbox.itemRenderSizeMultiplier,
                        info.immersiveDir, hitbox.getUpDownRenderDir(), hitbox.getAABB(), true, spinDegrees, info.light);
            } else {
                renderHitbox(stack, hitbox.getAABB(), hitbox.getPos());
            }
            for (TextData data : hitbox.getTextData()) {
                renderText(data.text(), stack, data.pos(), info.light);
            }
        }

    }

    @Override
    public boolean enabledInConfig() {
        return builder.enabledInConfigSupplier.get();
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(BuiltImmersiveInfo info, int inputSlotNum) {
        HitboxInfo hitbox = info.inputsToHitboxes.get(inputSlotNum);
        return (hitbox.item == null || hitbox.item.isEmpty()) && builder.slotRendersItemGuide.apply(info, inputSlotNum);
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return builder.blockChecker.apply(pos, state, tileEntity, level);
    }

    private boolean shouldTrack(BlockPos pos) {
        return shouldTrack(pos,
                Minecraft.getInstance().level.getBlockState(pos),
                Minecraft.getInstance().level.getBlockEntity(pos),
                Minecraft.getInstance().level);
    }

    @Override
    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (BuiltImmersiveInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(builder.renderTime);
                return;
            }
        }
        if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            BuiltImmersiveInfo info = new BuiltImmersiveInfo(builder.hitboxes, pos,
                    builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(HorizontalDirectionalBlock.FACING);
            this.infos.add(info);
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_PLAYER_FACING) {
          this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_LITERAL) {
          this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            BuiltImmersiveInfo info = new BuiltImmersiveInfo(builder.hitboxes, pos,
                    builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(HorizontalDirectionalBlock.FACING);
            this.infos.add(info);
        } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
            BuiltImmersiveInfo info = new BuiltImmersiveInfo(builder.hitboxes, pos,
                    builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(BlockStateProperties.FACING);
            this.infos.add(info);
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_NO_DOWN) {
            this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING) {
            this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else {
            throw new UnsupportedOperationException("Tracking for positioning mode " + builder.positioningMode + " unimplemented!");
        }
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return builder.blockRightClickWhenGUIClickDisabled;
    }

    @Override
    protected void initInfo(BuiltImmersiveInfo info) {
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        BuiltImmersiveInfo bInfo = (BuiltImmersiveInfo) info;
        // Regular hitboxes and trigger hitboxes are mixed together, so we need to filter out the
        // trigger hitboxes here (unless we aren't in VR, of course).
        if (!bInfo.hitboxes[closest].isTriggerHitbox || !VRPluginVerify.clientInVR()) {
            builder.rightClickHandler.apply((BuiltImmersiveInfo) info, player, closest, hand);
        }
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        BuiltImmersiveInfo bInfo = (BuiltImmersiveInfo) info;
        builder.rightClickHandler.apply(bInfo, player, bInfo.triggerToRegularHitbox.get(hitboxNum),
                InteractionHand.values()[bInfo.getVRControllerNum()]);
    }

    @Override
    public BlockPos getLightPos(BuiltImmersiveInfo info) {
        if (builder.lightPositionOffsets.isEmpty()) {
            if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
                return info.getBlockPosition().relative(info.immersiveDir);
            } else if (builder.positioningMode == HitboxPositioningMode.TOP_PLAYER_FACING) {
              return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.TOP_LITERAL) {
                return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
                return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
              return info.getBlockPosition().relative(AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player));
            } else if (builder.positioningMode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
                return info.getBlockPosition().relative(info.immersiveDir);
            } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_NO_DOWN) {
                return info.getBlockPosition().relative(getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition()));
            } else {
                throw new UnsupportedOperationException("Light pos for positioning mode " + builder.positioningMode + " unimplemented!");
            }
        } else {
            return info.getBlockPosition().offset(builder.lightPositionOffsets.get(0));
        }
    }

    protected boolean airCheck(BuiltImmersiveInfo info) {
        List<BlockPos> positions = new ArrayList<>();
        if (builder.airCheckPositionOffsets.isEmpty()) {
            if (info.immersiveDir == null) {
                return false;
            }
            positions.add(info.getBlockPosition().relative(info.immersiveDir));
        } else {
            for (Vec3i offset : builder.airCheckPositionOffsets) {
                positions.add(info.getBlockPosition().offset(offset));
            }
        }
        for (BlockPos pos : positions) {
            if (!Minecraft.getInstance().level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockPos[] getLightPositions(BuiltImmersiveInfo info) {
        if (builder.lightPositionOffsets.size() > 1) {
            BlockPos[] lightPositions = new BlockPos[builder.lightPositionOffsets.size()];
            int i = 0;
            for (Vec3i offset : builder.lightPositionOffsets) {
                lightPositions[i++] = info.getBlockPosition().offset(offset);
            }
            return lightPositions;
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING) {
            BlockPos[] lightPositions = new BlockPos[4];
            Direction.Axis ignored = Minecraft.getInstance().player.level().getBlockState(info.getBlockPosition()).getValue(DirectionalBlock.FACING).getAxis();
            int i = 0;
            for (Direction dir : Direction.values()) {
                if (dir.getAxis() != ignored) {
                    lightPositions[i++] = info.getBlockPosition().relative(dir);
                }
            }
            return lightPositions;
        } else {
            throw new UnsupportedOperationException("Multiple light pos for positioning mode " + builder.positioningMode + " unimplemented!");
        }

    }

    @Override
    public boolean hasMultipleLightPositions(BuiltImmersiveInfo info) {
        return builder.lightPositionOffsets.size() > 1 || builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING;
    }

    @Override
    public void processStorageFromNetwork(AbstractImmersiveInfo info, ImmersiveStorage storage) {
        BuiltImmersiveInfo bInfo = (BuiltImmersiveInfo) info;
        for (int i = 0; i < storage.getNumItems(); i++) {
            bInfo.itemHitboxes.get(i).item = storage.getItem(i);
        }
        if (builder.extraStorageConsumer != null) {
            builder.extraStorageConsumer.accept(storage, (BuiltImmersiveInfo) info);
        }
    }

    public BuiltImmersiveInfo findImmersive(BlockPos pos) {
        Objects.requireNonNull(pos);
        for (BuiltImmersiveInfo info : this.getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                return info;
            }
        }
        return null;
    }

    public ImmersiveBuilder getBuilderClone() {
        return builder.clone();
    }
}
