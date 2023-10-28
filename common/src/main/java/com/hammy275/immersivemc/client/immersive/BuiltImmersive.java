package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltHorizontalBlockInfo;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuiltImmersive extends AbstractImmersive<BuiltImmersiveInfo> {

    // TODO: Use pre-calculation so we stop depending on the builder directly.
    protected final ImmersiveBuilder builder;

    public BuiltImmersive(ImmersiveBuilder builder) {
        super(builder.renderTime);
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

        // TODO: Optionally use setHitboxes() here so we don't recalculate every tick
        for (HitboxInfo hitbox : info.hitboxes) {
            hitbox.recalculate(Minecraft.getInstance().level, info.getBlockPosition(), builder.positioningMode);
        }
        if (info instanceof BuiltHorizontalBlockInfo horizInfo) {
            horizInfo.dir = Minecraft.getInstance().level.getBlockState(info.getBlockPosition())
                    .getValue(HorizontalDirectionalBlock.FACING);
        }
    }

    @Override
    protected void render(BuiltImmersiveInfo info, PoseStack stack, boolean isInVR) {
        float size = builder.renderSize / info.getItemTransitionCountdown();
        Direction facing = null;
        if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            BuiltHorizontalBlockInfo horizInfo = (BuiltHorizontalBlockInfo) info;
            facing = horizInfo.dir;
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
            facing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            facing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
        } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            facing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
        }
        for (int i = 0; i < info.hitboxes.length; i++) {
            HitboxInfo hitbox = info.hitboxes[i];
            if (hitbox.holdsItems) {
                int spinDegrees = hitbox.itemSpins ? (int) (info.ticksActive % 100d * 3.6d) : -1;
                renderItem(hitbox.item, stack, hitbox.getPos(),
                        info.slotHovered == i ? size * 1.25f * hitbox.itemRenderSizeMultiplier : size * hitbox.itemRenderSizeMultiplier,
                        facing, hitbox.upDownRenderDir, hitbox.getAABB(), true, spinDegrees, info.light);
            } else {
                renderHitbox(stack, hitbox.getAABB(), hitbox.getPos());
            }
            if (hitbox.textSupplier != null) {
                Component text = hitbox.textSupplier.apply(info);
                if (text != null) {
                    renderText(text, stack, hitbox.getPos(), info.light);
                }
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
        return hitbox.item == null || hitbox.item.isEmpty();
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
            this.infos.add(new BuiltHorizontalBlockInfo(builder.hitboxes, pos,
                    state.getValue(HorizontalDirectionalBlock.FACING),
                    builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
          this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_LITERAL) {
          this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            this.infos.add(new BuiltHorizontalBlockInfo(builder.hitboxes, pos,
                    state.getValue(HorizontalDirectionalBlock.FACING),
                    builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime, builder.triggerHitboxControllerNum, builder.extraInfoDataClazz));
        } else {
            throw new UnsupportedOperationException("Tracking for positioning mode " + builder.positioningMode + " unimplemented!");
        }
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    protected void initInfo(BuiltImmersiveInfo info) {
        // TODO
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
                BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
                return info.getBlockPosition().relative(state.getValue(AbstractFurnaceBlock.FACING));
            } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
              return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.TOP_LITERAL) {
                return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
                return info.getBlockPosition().above();
            } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
              return info.getBlockPosition().relative(AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player));
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
            if (this.hasMultipleLightPositions(info)) {
                positions.addAll(Arrays.asList(getLightPositions(info)));
            } else {
                positions.add(this.getLightPos(info));
            }
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
        BlockPos[] lightPositions = new BlockPos[builder.lightPositionOffsets.size()];
        int i = 0;
        for (Vec3i offset : builder.lightPositionOffsets) {
            lightPositions[i++] = info.getBlockPosition().offset(offset);
        }
        return lightPositions;
    }

    @Override
    public boolean hasMultipleLightPositions(BuiltImmersiveInfo info) {
        return builder.lightPositionOffsets.size() > 1;
    }

    @Override
    public void processStorageFromNetwork(AbstractImmersiveInfo info, ImmersiveStorage storage) {
        BuiltImmersiveInfo bInfo = (BuiltImmersiveInfo) info;
        for (int i = 0; i < bInfo.itemHitboxes.size(); i++) {
            bInfo.itemHitboxes.get(i).item = storage.getItem(i);
        }
        if (builder.extraStorageConsumer != null) {
            builder.extraStorageConsumer.accept(storage, (BuiltImmersiveInfo) info);
        }
    }
}
