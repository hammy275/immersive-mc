package com.hammy275.immersivemc.forge.mixin;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    // Placed into each modloader-specific folder, since Forge changes the signature of the method.
    @Inject(method="renderBlockEntities", at = @At("RETURN"))
    private void immersiveMC$renderLevelWithParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource crumblingBufferSource, Camera camera, float partialTick, Frustum frustum, CallbackInfoReturnable<Boolean> ci) {
        Profiler.get().popPush(ImmersiveMC.MOD_ID);
        ClientRenderSubscriber.onWorldRender(poseStack);
    }
}
