package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method="renderLevel", at=
            @At(value = "INVOKE_STRING", args="ldc=fog", target="Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"))
    private void immersiveMC$renderLevelWithParticles(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        Profiler.get().popPush(ImmersiveMC.MOD_ID);
        PoseStack poseStack = new PoseStack();
        ClientRenderSubscriber.onWorldRender(poseStack);
    }
}
