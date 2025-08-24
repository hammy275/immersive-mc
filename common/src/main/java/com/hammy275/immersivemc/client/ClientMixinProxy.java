package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.mixin.ChestLidControllerAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.stream.StreamSupport;

public class ClientMixinProxy {

    public static boolean pretendPlayerIsNotCrouching = false;

    public static boolean playerIsLocalPlayer(Player player) {
        return player == Minecraft.getInstance().player;
    }

    public static boolean collideDoPlayerMoveInRoomRedirect(ClientLevel level, Entity entity, AABB aabb) {
        if (VRVerify.clientInVR() && ActiveConfig.getActiveConfigCommon((LocalPlayer) entity).dontAutoStepOnImmersiveBlocksInVR) {
            if (StreamSupport.stream(BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY - 0.500001), Mth.floor(aabb.minZ),
                            Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false)
                    .anyMatch(pos -> Util.blockIsActiveImmersive((LocalPlayer) entity, pos))) {
                return false; // Collide if trying to move on top of an Immersive block
            }
        }
        return level.noCollision(entity, aabb);
    }

    public static <T extends BlockEntity> void handleForcedLidAnimation(Level level, BlockPos pos, BlockState state, T blockEntity, Operation<Void> original) {
        if (level.isClientSide()) {
            ChestInfo info = ClientUtil.findImmersive(Immersives.immersiveChest, pos);
            if (info != null && info.forcedOpenness >= 0) {
                ChestLidControllerAccessor lidController = Util.getChestLidController(blockEntity);
                lidController.immersiveMC$setOldOpenness(lidController.immersiveMC$getOpenness());
                lidController.immersiveMC$setOpenness(info.forcedOpenness);
            } else {
                original.call(level, pos, state, blockEntity);
            }
        }
    }
}
