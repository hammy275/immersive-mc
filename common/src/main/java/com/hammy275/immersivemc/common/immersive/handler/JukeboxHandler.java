package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public class JukeboxHandler implements ImmersiveHandler<NullStorage> {
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
        if (player.level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            ItemStack playerItem = player.getItemInHand(hand);
            if (jukebox.getRecord().isEmpty() &&
                    playerItem.is(ItemTags.MUSIC_DISCS)) {
                ItemStack copiedItem = playerItem.copy();
                copiedItem.setCount(1);
                jukebox.setRecord(copiedItem);
                player.level.levelEvent(null, 1010, pos, Item.getId(playerItem.getItem()));
                playerItem.shrink(1);
                player.awardStat(Stats.PLAY_RECORD);
                jukebox.setChanged();
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return false; // Jukebox doesn't have data to sync to the client.
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof JukeboxBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useJukeboxImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "jukebox");
    }
}
