package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method="renderBlockEntities", at = @At("TAIL"))
    private void immersiveMC$renderLevelWithParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource crumblingBufferSource, Camera camera, float partialTick, CallbackInfo ci) {
        Profiler.get().popPush(ImmersiveMC.MOD_ID);
        ClientRenderSubscriber.onWorldRender(poseStack);
    }
}
