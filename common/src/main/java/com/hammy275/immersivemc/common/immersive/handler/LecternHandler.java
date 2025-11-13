package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.LecternData;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LecternHandler implements ImmersiveHandler<LecternData<CommonBookData>> {
    @Override
    public LecternData<CommonBookData> makeInventoryContents(ServerPlayer player, BlockPos pos) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().getOrCreate(player.level(), pos, this);
        LecternBlockEntity lectern = (LecternBlockEntity) player.level().getBlockEntity(pos);
        storage.setBook(lectern.getBook(), lectern);
        storage.pos = pos;
        storage.level = player.level();
        return storage;
    }

    @Override
    public LecternData<CommonBookData> getEmptyNetworkStorage() {
        return new LecternData<>(new CommonBookData());
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().get(player.level(), pos, this);
        if (storage != null && !storage.book.isEmpty()) {
            if (slot == 0) {
                storage.bookData.lastPage();
            } else {
                storage.bookData.nextPage();
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().get(player.level(), pos, this);
        return DirtyTracker.isDirty(player.level(), pos) ||
                storage != null && storage.bookData.isDirty();
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof LecternBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useLecternImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return false;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("lectern");
    }
}
