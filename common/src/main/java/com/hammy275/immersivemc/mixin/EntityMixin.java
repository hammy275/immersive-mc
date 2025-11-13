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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.StreamSupport;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow protected abstract Vec3 collide(Vec3 vec);

    @Shadow public float maxUpStep;

    @Inject(method = "collide", at = @At("RETURN"), cancellable = true)
    private void immersiveMC$differentCollideIfSteppingOntoImemrsive(Vec3 moveVecIn, CallbackInfoReturnable<Vec3> cir) {
        if (maxUpStep > 0.0001f && (Object) this instanceof Player player && ActiveConfig.getActiveConfigCommon(player).dontAutoStepOnImmersiveBlocksInVR
                && VRVerify.playerInVR(player) && !((LivingEntityAccessor) this).immersiveMC$jumping()) {
            if (StreamSupport.stream(player.level.getBlockCollisions(player, player.getBoundingBox().expandTowards(moveVecIn)).spliterator(), false)
                    .filter(shape -> !shape.isEmpty())
                    .map(shape -> new BlockPos(shape.bounds().getCenter()))
                    .anyMatch(pos -> Util.blockIsActiveImmersive(player, pos))) {
                Vec3 moveVecOut = cir.getReturnValue();
                if (moveVecOut.y > 0) {
                    float oldMaxUpStep = this.maxUpStep;
                    this.maxUpStep = 0.0001f;
                    cir.setReturnValue(this.collide(moveVecIn));
                    this.maxUpStep = oldMaxUpStep;
                }
            }
        }
    }

    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void immersiveMC$notCrouchingWhenUseChecking(CallbackInfoReturnable<Boolean> cir) {
        Entity me = (Entity) (Object) this;
        if (me.level.isClientSide() ? ClientMixinProxy.pretendPlayerIsNotCrouching : ServerMixinProxy.pretendPlayerIsNotCrouching) {
            cir.setReturnValue(false);
        }
    }
}
