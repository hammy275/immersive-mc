package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.LecternData;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LecternHandler implements BlockBasedImmersiveHandler<LecternData<CommonBookData>> {
    @Override
    public LecternData<CommonBookData> makeInventoryContents(ServerPlayer tracker, BlockPos pos) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().getOrCreate(tracker.level(), pos, this);
        LecternBlockEntity lectern = (LecternBlockEntity) tracker.level().getBlockEntity(pos);
        storage.setBook(lectern.getBook(), lectern);
        storage.pos = pos;
        storage.level = tracker.level();
        return storage;
    }

    @Override
    public LecternData<CommonBookData> getEmptyNetworkStorage() {
        return new LecternData<>(new CommonBookData());
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().get(tracker.level(), pos, this);
        if (storage != null && !storage.book.isEmpty()) {
            if (slot == 0) {
                storage.bookData.lastPage();
            } else {
                storage.bookData.nextPage();
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos) {
        LecternData<CommonBookData> storage = SharedNetworkStorages.instance().get(tracker.level(), pos, this);
        return DirtyTracker.isDirty(tracker.level(), pos) ||
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
    public Identifier getID() {
        return Util.id("lectern");
    }
}
