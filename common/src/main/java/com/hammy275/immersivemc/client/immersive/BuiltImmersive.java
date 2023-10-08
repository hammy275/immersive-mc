package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltHorizontalBlockInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    public boolean shouldRender(BuiltImmersiveInfo info, boolean isInVR) {
        return info.readyToRender() && builder.extraRenderReady.apply(info);
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
        if (info instanceof BuiltHorizontalBlockInfo horizInfo) {
            facing = horizInfo.dir;
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
            facing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
        }
        for (int i = 0; i < info.itemHitboxes.size(); i++) {
            HitboxInfo hitbox = info.itemHitboxes.get(i);
            int spinDegrees = hitbox.itemSpins ? (int) (info.ticksActive % 100d * 3.6d) : -1;
            renderItem(hitbox.item, stack, hitbox.getPos(),
                    info.slotHovered == i ? size * 1.25f * hitbox.itemRenderSizeMultiplier : size * hitbox.itemRenderSizeMultiplier,
                    facing, hitbox.upDownRenderDir, hitbox.getAABB(), true, spinDegrees, info.light);
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
                    builder.renderTime));
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
          this.infos.add(new BuiltImmersiveInfo(builder.hitboxes, pos, builder.renderTime));
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
        builder.rightClickHandler.apply((BuiltImmersiveInfo) info, player, closest, hand);
    }

    @Override
    public BlockPos getLightPos(BuiltImmersiveInfo info) {
        if (builder.lightPositionOffsets.isEmpty()) {
            if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
                BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
                return info.getBlockPosition().relative(state.getValue(AbstractFurnaceBlock.FACING));
            } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING) {
              return info.getBlockPosition().above();
            } else {
                throw new UnsupportedOperationException("Light pos for positioning mode " + builder.positioningMode + " unimplemented!");
            }
        } else {
            return info.getBlockPosition().offset(builder.lightPositionOffsets.get(0));
        }
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
    }
}
