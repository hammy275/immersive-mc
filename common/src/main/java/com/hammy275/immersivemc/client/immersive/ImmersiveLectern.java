package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.LecternInfo;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmersiveLectern implements Immersive<LecternInfo, BookData> {

    protected final List<LecternInfo> infos = new ArrayList<>();

    @Override
    public Collection<LecternInfo> getTrackedObjects() {
        return infos;
    }

    @Override
    public LecternInfo buildInfo(BlockPos pos, Level level) {
        return new LecternInfo(pos);
    }

    @Override
    public int handleHitboxInteract(LecternInfo info, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        // Handled in ClientBookData#tick()
        if (hitboxIndex <= 2) {
            return 0;
        } else {
            return info.bookData.doPageInteract(hand.ordinal()) ? 20 : -1;
        }
    }

    @Override
    public boolean shouldRender(LecternInfo info) {
        return !info.bookData.book.isEmpty() && info.bookData.pageTurnBoxes[2] != null && info.bookData.lecternPosRot != null;
    }

    @Override
    public void render(LecternInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        info.bookData.render(stack, info.bookData.lecternPosRot);
    }

    @Override
    public void tick(LecternInfo info) {
        info.tickCount++;
        info.bookData.lecternPlayerTick(Minecraft.getInstance().player, info.getBlockPosition());
    }

    @Override
    public ImmersiveHandler<BookData> getHandler() {
        return ImmersiveHandlers.lecternHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("lectern", () -> new ItemStack(Items.LECTERN), ImmersiveMCConfig.useLecternImmersion);
    }

    @Override
    public boolean shouldDisableRightClicksWhenInteractionsDisabled(LecternInfo info) {
        return false;
    }

    @Override
    public void processStorageFromNetwork(LecternInfo info, BookData storage) {
        info.bookData.processFromNetwork(storage);
    }

    @Override
    public boolean isVROnly() {
        return false;
    }
}
