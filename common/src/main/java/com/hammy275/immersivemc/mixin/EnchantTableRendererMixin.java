package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
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

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/EnchantmentTableBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
    at = @At("HEAD"), cancellable = true)
    private void immersiveMC$apothEnchantTableBook(EnchantmentTableBlockEntity table, float partialTick,
                                                   PoseStack stack, MultiBufferSource buffer,
                                                   int light, int packedOverlay, CallbackInfo ci) {
        if (Apoth.apothImpl.enchantModuleEnabled() && table.open == 1f) {
            Optional<BuiltImmersiveInfo<EnchantingData>> infoOpt = Immersives.immersiveETable.getTrackedObjects().stream()
                    .filter(i -> i.getBlockPosition().equals(table.getBlockPos()))
                    .findFirst();
            if (infoOpt.isPresent()) {
                BlockPos pos = table.getBlockPos();
                Player player = Minecraft.getInstance().level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
                if (player != null) {
                    BuiltImmersiveInfo<EnchantingData> info = infoOpt.get();
                    ClientBookData bookData = info.getExtraData().getBookData(info.getItem(0));
                    if (bookData != null) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
