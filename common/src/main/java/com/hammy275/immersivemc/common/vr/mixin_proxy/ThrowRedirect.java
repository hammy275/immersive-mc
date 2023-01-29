package com.hammy275.immersivemc.common.vr.mixin_proxy;

import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.server.data.AboutToThrowData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class ThrowRedirect {

    public static void shootFromRotation(Projectile projectile,
                                         Entity shooter, float xAngle, float yAngle, float unknown, float velocity, float inaccuracy) {
        if (shooter instanceof Player player) {
            if (VRPlugin.API.playerInVR(player) && AboutToThrowData.aboutToThrowMap.containsKey(player.getUUID())) {
                AboutToThrowData.ThrowRecord data = AboutToThrowData.aboutToThrowMap.remove(player.getUUID());
                projectile.shoot(data.dir().x, data.dir().y, data.dir().z, velocity, inaccuracy);
                Vec3 shooterVelocity = shooter.getDeltaMovement();
                projectile.setDeltaMovement(
                        projectile.getDeltaMovement()
                                .add(shooterVelocity.x, shooter.isOnGround() ? 0.0D : shooterVelocity.y, shooterVelocity.z)
                );

                // Don't call normal shoot logic
                return;
            }
        }

        projectile.shootFromRotation(shooter, xAngle, yAngle, unknown, velocity, inaccuracy);

    }
}
