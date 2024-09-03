package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.BuiltImmersive;
import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxPositioningMode;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfoImpl;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class BuiltImmersiveImpl<E, S extends NetworkStorage> implements BuiltImmersive<E, S> {

    private final ImmersiveBuilderImpl<E, S> builder;
    private final List<BuiltImmersiveInfo<E>> infos = new ArrayList<>();

    public BuiltImmersiveImpl(ImmersiveBuilderImpl<E, S> builder) {
        this.builder = builder;
    }

    @Override
    public ImmersiveHandler<S> getHandler() {
        return builder.handler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return builder.configScreenInfo;
    }

    @Override
    public boolean shouldRender(BuiltImmersiveInfo<E> infoIn) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        return getHandler().isValidBlock(info.getBlockPosition(), Minecraft.getInstance().level) &&
                        info.hasHitboxes() && info.airCheckPassed && builder.extraRenderReady.apply(info);
    }

    @Override
    public void tick(BuiltImmersiveInfo<E> infoIn) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        info.ticksExisted++;
        Direction currentDir;
        switch (builder.positioningMode) {
            case HORIZONTAL_BLOCK_FACING, BLOCK_FACING_NEG_X ->
                    currentDir = info.immersiveDir;
            case TOP_PLAYER_FACING, TOP_BLOCK_FACING, HORIZONTAL_PLAYER_FACING, PLAYER_FACING_NO_DOWN ->
                    currentDir = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition());
            case TOP_LITERAL ->
                    currentDir = null;
            case PLAYER_FACING_FILTER_BLOCK_FACING ->
                    currentDir = Util.getForwardFromPlayerUpAndDownFilterBlockFacing(Minecraft.getInstance().player, info.getBlockPosition(), true);
            default ->
                    throw new UnsupportedOperationException("Facing direction for positioning mode " + builder.positioningMode + " unimplemented!");

        }

        boolean differentDirs = info.immersiveDir != currentDir;
        info.immersiveDir = currentDir;

        for (int i = 0; i < info.hitboxes.size(); i++) {
            RelativeHitboxInfoImpl hitbox = info.hitboxes.get(i);
            // Update hitbox if its offset isn't constant, the current direction isn't the same as the last,
            // if it hasn't been calculated yet, if slots can change whether they're active, or if they need
            // to detect VR hand movements.
            if (!hitbox.constantOffset || differentDirs || !hitbox.calcDone() || builder.slotActive != null
                || hitbox.vrMovementInfo != null) {
                if (builder.slotActive == null || builder.slotActive.apply(info, i)) {
                    hitbox.recalculate(Minecraft.getInstance().level, builder.positioningMode, info);
                } else {
                    // Force hitbox to be null if not active. Prevents rendering and hitbox collision, but still keeps
                    // the slot available to receive items from the network, etc.
                    hitbox.forceNull();
                }
            }
        }

        info.airCheckPassed = airCheck(info);
        info.light = ImmersiveClientLogicHelpers.instance().getLight(getLightPositions(info));
    }

    @Override
    public void render(BuiltImmersiveInfo<E> infoIn, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        float size = ImmersiveRenderHelpers.instance().getTransitionMultiplier(info.ticksExisted) * builder.renderSize;
        for (int i = 0; i < info.hitboxes.size(); i++) {
            RelativeHitboxInfoImpl hitbox = info.hitboxes.get(i);
            // Built Immersives can give null hitboxes to skip rendering them. Need to make sure it's nonnull before
            // trying to render it.
            if (hitbox.hasAABB()) {
                if (hitbox.holdsItems && hitbox.renderItem) {
                    Float spinDegrees = hitbox.itemSpins ? info.ticksExisted % 100f * 3.6f : null;
                    if (hitbox.item == null || hitbox.item.isEmpty()) {
                        if (hitbox.isInput && builder.slotRendersItemGuide.apply(info, i)) {
                            helpers.renderItemGuide(stack, hitbox.getHitbox(), info.isSlotHovered(i), info.light);
                        }
                    } else {
                        float renderSize = size * hitbox.itemRenderSizeMultiplier;
                        if (info.isSlotHovered(i)) {
                            renderSize *= ImmersiveRenderHelpers.instance().hoverScaleSizeMultiplier();
                        }
                        helpers.renderItem(hitbox.item, stack, renderSize,
                                hitbox.getHitbox(), hitbox.renderItemCount, info.light, spinDegrees, info.immersiveDir,
                                hitbox.getUpDownRenderDir());
                    }
                } else {
                    helpers.renderHitbox(stack, hitbox.getAABB());
                }
                for (TextData data : hitbox.getTextData()) {
                    helpers.renderText(data.text(), stack, data.pos(), info.light, 0.02f);
                }
            }

        }

    }

    @Override
    public BuiltImmersiveInfo<E> buildInfo(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);
        BuiltImmersiveInfoImpl<E> info;
        if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(HorizontalDirectionalBlock.FACING);
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_PLAYER_FACING) {
          info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_LITERAL) {
          info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
        } else if (builder.positioningMode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos,
                    builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(HorizontalDirectionalBlock.FACING);
        } else if (builder.positioningMode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
        } else if (builder.positioningMode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos,
                    builder.extraInfoDataClazz);
            info.immersiveDir = state.getValue(BlockStateProperties.FACING);
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_NO_DOWN) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING) {
            info = new BuiltImmersiveInfoImpl<>(builder.hitboxes, pos, builder.extraInfoDataClazz);
        } else {
            throw new UnsupportedOperationException("Tracking for positioning mode " + builder.positioningMode + " unimplemented!");
        }
        return info;
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(BuiltImmersiveInfo<E> info) {
        return builder.blockRightClickWhenGUIClickDisabled;
    }


    @Override
    public Collection<BuiltImmersiveInfo<E>> getTrackedObjects() {
        return this.infos;
    }

    @Override
    public int handleHitboxInteract(BuiltImmersiveInfo<E> infoIn, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        return builder.hitboxInteractHandler.apply(info, player, hitboxIndex, hand);
    }

    private boolean airCheck(BuiltImmersiveInfo<E> infoIn) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        Collection<BlockPos> positions;
        if (builder.airCheckPositionOffsets.isEmpty()) {
            positions = new HashSet<>();
            info.hitboxes.forEach((hitbox) -> {
                if (hitbox.hasAABB()) {
                    positions.addAll(Util.allPositionsWithAABB(hitbox.getAABB()));
                }
            });
            BlockPos immersivePos = info.getBlockPosition();
            positions.remove(immersivePos);
            // Remove checking for air all positions >1 block out on any individual axis. This is done so things
            // like the Enchanting Table Immersive's animations, which barely clip into the next block on the Y-axis,
            // aren't checked for the air check.
            positions.removeIf((pos) -> (Math.abs(pos.getX() - immersivePos.getX()) > 1 ||
                    Math.abs(pos.getY() - immersivePos.getY()) > 1 ||
                    Math.abs(pos.getZ() - immersivePos.getZ()) > 1));
        } else {
            positions = new ArrayList<>();
            for (Vec3i offset : builder.airCheckPositionOffsets) {
                positions.add(info.getBlockPosition().offset(offset));
            }
        }
        for (BlockPos pos : positions) {
            if (!Minecraft.getInstance().level.getBlockState(pos).getMaterial().isReplaceable()) {
                return false;
            }
        }
        return true;
    }

    public List<BlockPos> getLightPositions(BuiltImmersiveInfo<E> info) {
        if (builder.lightPositionOffsets.size() > 1) {
            List<BlockPos> lightPositions = new ArrayList<>();
            for (Vec3i offset : builder.lightPositionOffsets) {
                lightPositions.add(info.getBlockPosition().offset(offset));
            }
            return lightPositions;
        } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING) {
            List<BlockPos> lightPositions = new ArrayList<>();
            Direction.Axis ignored = Minecraft.getInstance().player.level.getBlockState(info.getBlockPosition()).getValue(DirectionalBlock.FACING).getAxis();
            for (Direction dir : Direction.values()) {
                if (dir.getAxis() != ignored) {
                    lightPositions.add(info.getBlockPosition().relative(dir));
                }
            }
            return lightPositions;
        } else {
            return List.of(getLightPos(info));
        }
    }

    public BlockPos getLightPos(BuiltImmersiveInfo<E> infoIn) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
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
                return info.getBlockPosition().relative(ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()));
            } else if (builder.positioningMode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
                return info.getBlockPosition().relative(info.immersiveDir);
            } else if (builder.positioningMode == HitboxPositioningMode.PLAYER_FACING_NO_DOWN) {
                return info.getBlockPosition().relative(Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition()));
            } else {
                throw new UnsupportedOperationException("Light pos for positioning mode " + builder.positioningMode + " unimplemented!");
            }
        } else {
            return info.getBlockPosition().offset(builder.lightPositionOffsets.get(0));
        }
    }

    @Override
    public void processStorageFromNetwork(BuiltImmersiveInfo<E> infoIn, S storage) {
        BuiltImmersiveInfoImpl<E> info = asImpl(infoIn);
        if (storage instanceof ListOfItemsStorage itemsStorage) {
            for (int i = 0; i < itemsStorage.getItems().size(); i++) {
                info.hitboxes.get(i).item = itemsStorage.getItems().get(i);
            }
        } else if (storage instanceof ItemStorage iws) {
            for (int i = 0; i < iws.getNumItems(); i++) {
                info.hitboxes.get(i).item = iws.getItem(i);
            }
        }
        if (builder.extraStorageConsumer != null) {
            builder.extraStorageConsumer.accept(storage, (BuiltImmersiveInfoImpl) info);
        }
    }

    @Override
    public boolean isVROnly() {
        return builder.vrOnly;
    }

    public <T extends NetworkStorage> ImmersiveBuilderImpl<E, T> getBuilderClone(ImmersiveHandler<T> newHandler) {
        return builder.copy(newHandler);
    }

    @SuppressWarnings("unchecked")
    private BuiltImmersiveInfoImpl<E> asImpl(BuiltImmersiveInfo<E> infoIn) {
        return (BuiltImmersiveInfoImpl<E>) infoIn;
    }
}
