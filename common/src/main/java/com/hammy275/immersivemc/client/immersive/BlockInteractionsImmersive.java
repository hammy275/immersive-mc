package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.info.BlockInteractionsImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockInteractionsImmersive extends AbstractImmersive<BlockInteractionsImmersiveInfo, NullStorage> {
    @Override
    public BlockInteractionsImmersiveInfo buildInfo(BlockPos pos, Level level) {
        return new BlockInteractionsImmersiveInfo(pos);
    }

    @Override
    public int handleHitboxInteract(BlockInteractionsImmersiveInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        return -1;
    }

    @Override
    public @Nullable BoundingBox getDragHitbox(BlockInteractionsImmersiveInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(BlockInteractionsImmersiveInfo info, int hitboxIndex) {
        return false;
    }

    @Override
    public boolean shouldRender(BlockInteractionsImmersiveInfo info) {
        return false;
    }

    @Override
    public void render(BlockInteractionsImmersiveInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {

    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.nullHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return null;
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(BlockInteractionsImmersiveInfo info) {
        return true; // To block vanilla interactions on blocks we want to
    }

    @Override
    public void processStorageFromNetwork(BlockInteractionsImmersiveInfo info, NullStorage storage) {

    }

    @Override
    public boolean isVROnly() {
        return false;
    }
}
