package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {

    @Shadow @Final public BookModel bookModel;

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EnchantTableRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
    at = @At("HEAD"), cancellable = true)
    private void immersiveMC$apothEnchantTableBook(EnchantTableRenderState table, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (Apoth.apothImpl.enchantModuleEnabled() && table.open == 1f) {
            BlockPos pos = table.blockPos;
            Optional<BuiltImmersiveInfo<EnchantingData>> infoOpt = Immersives.immersiveETable.getTrackedObjects().stream()
                    .filter(i -> i.getBlockPosition().equals(pos))
                    .findFirst();
            if (infoOpt.isPresent()) {
                Player player = Minecraft.getInstance().level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
                if (player != null) {
                    BuiltImmersiveInfo<EnchantingData> info = infoOpt.get();
                    ClientBookData bookData = info.getExtraData().getBookData(info);
                    if (bookData != null) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
