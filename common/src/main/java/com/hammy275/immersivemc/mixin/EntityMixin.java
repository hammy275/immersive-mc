package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.ServerMixinProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    private static Entity me;

    @Inject(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collectCandidateStepUpHeights(Lnet/minecraft/world/phys/AABB;Ljava/util/List;FF)[F"))
    private void immersiveMC$beforeCollectStepCandidates(Vec3 vec, CallbackInfoReturnable<Vec3> cir) {
        me = (Entity) (Object) this;
    }

    @Inject(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;collectCandidateStepUpHeights(Lnet/minecraft/world/phys/AABB;Ljava/util/List;FF)[F"))
    private void immersiveMC$afterCollectStepCandidates(Vec3 vec, CallbackInfoReturnable<Vec3> cir) {
        me = null;
    }

    @ModifyVariable(method = "collectCandidateStepUpHeights", at = @At("HEAD"), index = 1, ordinal = 0, argsOnly = true)
    private static List<VoxelShape> immersiveMC$removeImmersiveBlocksFromStepUpCandidates(List<VoxelShape> original) {
        if (me instanceof Player player && ActiveConfig.getActiveConfigCommon(player).dontAutoStepOnImmersiveBlocksInVR
            && VRVerify.playerInVR(player)) {
            return original.stream().filter(shape -> {
                if (!shape.isEmpty()) {
                    BlockPos pos = BlockPos.containing(shape.bounds().getCenter());
                    return !Util.blockIsActiveImmersive(player, pos);
                }
                return true;
            }).toList();
        }
        return original;
    }

    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void immersiveMC$notCrouchingWhenUseChecking(CallbackInfoReturnable<Boolean> cir) {
        Entity me = (Entity) (Object) this;
        if (me.level().isClientSide() ? ClientMixinProxy.pretendPlayerIsNotCrouching : ServerMixinProxy.pretendPlayerIsNotCrouching) {
            cir.setReturnValue(false);
        }
    }
}
