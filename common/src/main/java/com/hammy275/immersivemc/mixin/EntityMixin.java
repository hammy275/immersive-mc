package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.StreamSupport;

@Mixin(Entity.class)
public class EntityMixin {

    @Redirect(method = "move",
        at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/phys/Vec3;lengthSqr()D"))
    private double immersiveMC$cancelMovementIfMovingUpToAnImmersiveBlock(Vec3 moveVec) {
        // If we're going up, any block in our new hitbox or any block -0.500001 blocks below our hitbox contains an
        // active Immersive, and we aren't jumping, don't move.
        if (moveVec.y >= 0.125 && (Object) this instanceof Player player && ActiveConfig.getActiveConfigCommon(player).dontAutoStepOnImmersiveBlocksInVR
            && VRPluginVerify.playerInVR(player) && !((LivingEntityAccessor) this).immersiveMC$jumping()) {
            AABB aabb = player.getBoundingBox().move(moveVec);
            if (StreamSupport.stream(BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY - 0.500001), Mth.floor(aabb.minZ),
                            Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false)
                    .anyMatch(pos -> Util.blockIsActiveImmersive(player, pos))) {
                return 0;
            }
        }
        return moveVec.lengthSqr();
    }
}
