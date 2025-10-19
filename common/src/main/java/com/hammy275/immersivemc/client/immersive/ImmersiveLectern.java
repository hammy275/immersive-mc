package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import com.hammy275.immersivemc.client.immersive.info.LecternInfo;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.LecternData;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmersiveLectern implements Immersive<LecternInfo, LecternData<CommonBookData>> {

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
    public int handleHitboxInteract(LecternInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        int hitboxIndex = hitboxIndices.get(0);
        if (hitboxIndex <= 2) {
            if (VRVerify.clientInVR()) { // Page turning handled in ClientBookData#tick() for VR players
                return 0;
            } else if (hitboxIndex <= 1) {
                Network.INSTANCE.sendToServer(new PageTurnPacket(info.getBlockPosition(), hitboxIndex == 1));
                return ImmersiveClientConstants.instance().defaultCooldown();
            }
        } else if (!VRVerify.clientInVR()) {
            // Text interaction is done in tick() instead for VR players
            info.lecternData.bookData.interactables.get(hitboxIndex - 3).interact(info.lecternData.bookData,
                    info.lecternData.getLecternPosRot(info.getBlockPosition()), null);
            return ImmersiveClientConstants.instance().defaultCooldown();
        }
        return -1;
    }

    @Override
    public boolean shouldRender(LecternInfo info) {
        return !info.lecternData.book.isEmpty() && info.light > -1 && getHandler().isValidBlock(info.getBlockPosition(), Minecraft.getInstance().level);
    }

    @Override
    public void render(LecternInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        info.lecternData.bookData.render(stack, info.light, info.lecternData.getLecternPosRot(info.getBlockPosition()));
    }

    @Override
    public void tick(LecternInfo info) {
        info.tickCount++;
        info.lecternData.bookData.interactables.clear();
        PosRot lecternPosRot = info.lecternData.getLecternPosRot(info.getBlockPosition());
        WrittenBookHelpers.addInteractablesForThisTick(info, lecternPosRot, true);
        WrittenBookHelpers.addInteractablesForThisTick(info, lecternPosRot, false);
        info.lecternData.tick(Minecraft.getInstance().player);
        info.light = ImmersiveClientLogicHelpers.instance().getLight(info.getBlockPosition().above());
        if (info.didClick) {
            ImmersiveClientLogicHelpers.instance().setCooldown(ImmersiveClientConstants.instance().defaultCooldown());
            info.didClick = false;
        }
    }

    @Override
    @Nullable
    public AABB getDragHitbox(LecternInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(LecternInfo info, int hitboxIndex) {
        return true;
    }

    @Override
    public ImmersiveHandler<LecternData<CommonBookData>> getHandler() {
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
    public void processStorageFromNetwork(LecternInfo info, LecternData<CommonBookData> storage) {
        info.setBook(storage.book);
        info.lecternData.mergeFromServer(storage);
        tick(info); // Do a tick so hitboxes are generated
        info.tickCount--; // Subtract extra tick count
    }

    @Override
    public boolean isVROnly() {
        return false;
    }
}
