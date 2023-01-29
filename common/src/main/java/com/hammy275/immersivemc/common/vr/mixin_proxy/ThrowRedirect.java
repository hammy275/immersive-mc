package com.hammy275.immersivemc.common.vr.mixin_proxy;

import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.server.data.AboutToThrowData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class ThrowRedirect {

    private static final double minThrowingVelocity = 0.01;
    private static final double mediumThrowingVelocity = 0.175;
    private static final double maxThrowingVelocity = 0.25;

    public static void shootFromRotation(Projectile projectile,
                                         Entity shooter, float xAngle, float yAngle, float unknown, float velocity, float inaccuracy) {
        if (shooter instanceof Player player) {
            if (VRPlugin.API.playerInVR(player) && AboutToThrowData.aboutToThrowMap.containsKey(player.getUUID())) {
                AboutToThrowData.ThrowRecord data = AboutToThrowData.aboutToThrowMap.remove(player.getUUID());

                // Modify velocity based on how hard the throw was
                double controllerVelocity = data.velocity().length();

                // Get a number to multiply velocity by between 0 and 1
                double projVelocityMod = controllerVelocity > mediumThrowingVelocity ?
                        Math.sin(mediumThrowingVelocity * Math.PI / 2 / 0.175) : Math.sin(controllerVelocity * Math.PI / 2 / 0.175);

                // Add an extra modifier based on how much faster we are than the throw
                double extraMod = (controllerVelocity - mediumThrowingVelocity) / 4;
                extraMod = Math.min(extraMod, 0.05);
                projVelocityMod += extraMod;

                // Force inaccuracy to be 0. Inaccuracy already comes from the player using a physical hand.
                projectile.shoot(data.dir().x, data.dir().y, data.dir().z, velocity * (float) projVelocityMod, 0);

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
