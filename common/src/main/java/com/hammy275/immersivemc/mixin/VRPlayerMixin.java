package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.gameplay.VRPlayer;

@Mixin(VRPlayer.class)
public class VRPlayerMixin {

    @Redirect(method = "doPlayerMoveInRoom",
            at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/multiplayer/ClientLevel;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean immersiveMC$collideIfWouldBeAboveImmersiveBlock(ClientLevel level, Entity entity, AABB aabb) {
        return ClientMixinProxy.collideDoPlayerMoveInRoomRedirect(level, entity, aabb);
    }
}
