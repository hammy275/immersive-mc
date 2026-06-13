package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    @WrapMethod(method = "addPairing")
    private void immersiveMC$addPairing(ServerPlayer player, Operation<Void> original) {
        original.call(player);
        if (this.entity instanceof ServerPlayer tracked) {
            TrackedImmersives.PLAYER_TRACKING.put(player, tracked);
        }
    }

    @WrapMethod(method = "removePairing")
    private void immersiveMC$removePairing(ServerPlayer player, Operation<Void> original) {
        original.call(player);
        if (this.entity instanceof ServerPlayer tracked) {
            TrackedImmersives.PLAYER_TRACKING.remove(player, tracked);
        }
    }
}
