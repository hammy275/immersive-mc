package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.server.storage.server.ItemSwapAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.hammy275.immersivemc.common.immersive.handler.CraftingHandler.isCraftingTableBlock;

public class BlockInteractionsImmersiveHandler implements ImmersiveHandler<NullStorage> {
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

    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        // lol, lmao
        Block block = level.getBlockState(pos).getBlock();
        return isCraftingTableBlock(block) && level.getBlockEntity(pos) == null;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return true;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "null");
    }
}
