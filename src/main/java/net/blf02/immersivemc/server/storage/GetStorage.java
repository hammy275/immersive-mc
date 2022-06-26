package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.server.storage.info.ImmersiveStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class GetStorage {

    public static int getLastInputIndex(BlockState state) {
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            return 8;
        }
        return -1;
    }

    public static ImmersiveStorage getStorage(PlayerEntity player, BlockPos pos) {
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            return getCraftingStorage(player, pos);
        }
        return null;
    }

    public static ImmersiveStorage getCraftingStorage(PlayerEntity player, BlockPos pos) {
        return WorldStorage.getStorage(player).getOrCreate(pos).initIfNotAlready(10);
    }
}
