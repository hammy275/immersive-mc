package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.LecternInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
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
        if (hitboxIndex <= 2) {
            if (VRPluginVerify.clientInVR()) { // Page turning handled in ClientBookData#tick() for VR players
                return 0;
            } else if (hitboxIndex <= 1) {
                Network.INSTANCE.sendToServer(new PageTurnPacket(info.getBlockPosition(), hitboxIndex == 1));
                return ClientConstants.defaultCooldownTicks;
            }
        } else if (!VRPluginVerify.clientInVR()) {
            // Text interaction is done in tick() instead for VR players
            info.bookData.doPageInteract(info.bookData.clickInfos.get(hitboxIndex - 3));
            return ClientConstants.defaultCooldownTicks;
        }
        return -1;
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
        if (VRPluginVerify.clientInVR() && Minecraft.getInstance().options.keyAttack.isDown()) {
            boolean didInteract = info.bookData.doPageInteract(0);
            if (didInteract) {
                ImmersiveClientLogicHelpers.instance().setCooldown((int) (ClientConstants.defaultCooldownTicks * ClientConstants.cooldownVRMultiplier));
            }
        }
    }

    @Override
    public ImmersiveHandler<BookData> getHandler() {
        return ImmersiveHandlers.lecternHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("lectern", () -> new ItemStack(Items.LECTERN),
                config -> config.useLecternImmersive,
                (config, newVal) -> config.useLecternImmersive = newVal);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(LecternInfo info) {
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
