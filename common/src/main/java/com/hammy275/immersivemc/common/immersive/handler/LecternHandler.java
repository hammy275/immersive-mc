package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.api.server.WorldStorages;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LecternHandler implements WorldStorageHandler<BookData> {
    @Override
    public BookData makeInventoryContents(ServerPlayer player, BlockPos pos) {
        BookData storage = (BookData) WorldStorages.instance().getOrCreate(pos, player.serverLevel());
        storage.authoritative = true;
        storage.book = ((LecternBlockEntity) player.level().getBlockEntity(pos)).getBook();
        storage.pos = pos;
        return storage;
    }

    @Override
    public BookData getEmptyNetworkStorage() {
        return new BookData(true);
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        BookData storage = (BookData) WorldStorages.instance().getOrCreate(pos, player.serverLevel());
        if (storage != null && !storage.book.isEmpty()) {
            if (slot == 0) {
                storage.lastPage();
            } else {
                storage.nextPage();
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        BookData storage = (BookData) WorldStorages.instance().getOrCreate(pos, player.serverLevel());
        return DirtyTracker.isDirty(player.level(), pos) ||
                storage != null && storage.isDirty();
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof LecternBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useLecternImmersion;
    }

    @Override
    public boolean clientAuthoritative() {
        return false;
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new BookData();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return BookData.class;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "lectern");
    }
}
