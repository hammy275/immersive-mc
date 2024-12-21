package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixin {

    /**
     * Used to allow the using of items at an arbitrary position, rather than where the player is looking.
     */
    @Redirect(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEyePosition()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 immersiveMC$changeEyePosition(Player player) {
        if (Util.activeUseInfo != null) {
            return Util.activeUseInfo.getVec3Pos();
        }
        return player.getEyePosition();
    }
}
