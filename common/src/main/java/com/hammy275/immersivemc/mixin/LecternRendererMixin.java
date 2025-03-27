package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.immersive.Immersives;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternRenderer.class)
public class LecternRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/LecternBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/phys/Vec3;)V",
    at = @At("HEAD"), cancellable = true)
    private void immersiveMC$onRenderStart(LecternBlockEntity lecternBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci) {
        if (Immersives.immersiveLectern.getTrackedObjects().stream()
                .anyMatch(info -> info.getBlockPosition().equals(lecternBlockEntity.getBlockPos()))) {
            ci.cancel();
        }
    }
}
