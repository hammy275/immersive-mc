package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookshelfHandler extends ContainerHandler<NullStorage> {
    public static final Vec3[] slotOffsets = new Vec3[]{
            new Vec3(-0.3125, 0.25, 0),
            new Vec3(0.03125, 0.25, 0),
            new Vec3(0.34375, 0.25, 0),
            new Vec3(-0.3125, -0.25, 0),
            new Vec3(0.03125, -0.25, 0),
            new Vec3(0.34375, -0.25, 0)
    };

    @Override
    public NullStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public NullStorage getEmptyNetworkStorage() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        BlockState state = player.level().getBlockState(pos);
        if (state.getBlock() instanceof ChiseledBookShelfBlock block) {
            Direction blockFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Vec3 blockEdgePos = Vec3.atBottomCenterOf(pos).add(blockFacing.getUnitVec3().scale(0.5)).add(0, 0.5, 0);
            // Change the offsets based on block facing:
            // +Z (south) = do not touch
            // -Z (north) = swap x/z signs
            // +X (east) = swap x/z signs and x/z axes
            // -X (west)  = swap x/z axes
            Vec3 offset = slotOffsets[slot];
            // Swap signs
            if (blockFacing == Direction.NORTH || blockFacing == Direction.EAST) {
                offset = offset.multiply(-1, 1, -1);
            }
            // Swap axes
            if (blockFacing.getAxis() == Direction.Axis.X) {
                offset = new Vec3(offset.z, offset.y, offset.x);
            }
            ItemStack stack = player.getItemInHand(hand);
            // Hit below gets overriden by mixins, but we need a valid one for some vanilla code that comes first.
            BlockHitResult hit = new BlockHitResult(blockEdgePos.add(offset), blockFacing, pos, false);
            if (stack.isEmpty()) {
                state.useWithoutItem(player.level(), player, hit);
            } else {
                state.useItemOn(stack, player.level(), player, hand, hit);
            }
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useChiseledBookshelfImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("chiseled_bookshelf");
    }
}
