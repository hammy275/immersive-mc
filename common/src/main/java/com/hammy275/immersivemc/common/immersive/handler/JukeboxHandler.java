package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.NullStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JukeboxHandler implements ImmersiveHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (player.level().getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            ItemStack playerItem = player.getItemInHand(hand);
            if (jukebox.getTheItem().isEmpty() &&
                    playerItem.is(ItemTags.MUSIC_DISCS)) {
                jukebox.setTheItem(playerItem.copyWithCount(1));
                playerItem.shrink(1);
                player.awardStat(Stats.PLAY_RECORD);
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return false; // Jukebox has no items to sync to the client
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        // No-op. Jukebox doesn't have data to sync to the client.
    }

    @Override
    public boolean usesWorldStorage() {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
        return blockEntity instanceof JukeboxBlockEntity;
    }

    @Override
    public boolean enabledInServerConfig() {
        return ActiveConfig.FILE.useJukeboxImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "jukebox");
    }
}
