package com.hammy275.immersivemc.common.vr.mixin_proxy;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.data.AboutToThrowData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ThrowRedirect {

    private static final double minThrowingVelocity = 0.01;
    private static final double mediumThrowingVelocity = 0.175;
    private static final double maxThrowingVelocity = 0.25;

    public static <T extends Projectile> T throwRedirect(Projectile.ProjectileFactory<T> factory, ServerLevel level,
                                                         ItemStack spawnedFrom, LivingEntity owner, float z,
                                                         float velocity, float inaccuracy) {
        T proj = null;
        if (VRVerify.hasAPI) {
            proj = ThrowRedirect.shootFromRotation(factory, level, spawnedFrom, owner, velocity);
        }
        if (proj == null) {
            return Projectile.spawnProjectileFromRotation(factory, level, spawnedFrom, owner, z, velocity, inaccuracy);
        } else {
            return proj;
        }
    }

    @Nullable
    public static <T extends Projectile> T shootFromRotation(Projectile.ProjectileFactory<T> factory, ServerLevel level,
                                            ItemStack spawnedFrom, LivingEntity shooter, float velocity) {
        if (shooter instanceof Player player) {
            if (VRVerify.playerInVR(player) && AboutToThrowData.aboutToThrowMap.containsKey(player.getUUID())) {
                T projectile = factory.create(level, shooter, spawnedFrom);
                AboutToThrowData.ThrowRecord data = AboutToThrowData.aboutToThrowMap.remove(player.getUUID());

                // Force inaccuracy to be 0. Inaccuracy already comes from the player using a physical hand.
                projectile.shoot(data.dir().x, data.dir().y, data.dir().z, velocity * (float) getVelocityMod(data), 0);

                Vec3 shooterVelocity = shooter.getDeltaMovement();
                projectile.setDeltaMovement(
                        projectile.getDeltaMovement()
                                .add(shooterVelocity.x, shooter.onGround() ? 0.0D : shooterVelocity.y, shooterVelocity.z)
                );
                level.addFreshEntity(projectile);
                if (spawnedFrom != null) {
                    projectile.applyOnProjectileSpawned(level, spawnedFrom);
                }
                return projectile;
            }
        }
        return null;
    }

    public static void deleteRecord(Player player) {
        if (VRVerify.playerInVR(player)) {
            AboutToThrowData.aboutToThrowMap.remove(player.getUUID());
        }
    }

    public static double getVelocityMod(AboutToThrowData.ThrowRecord data) {
        // Modify velocity based on how hard the throw was
        double controllerVelocity = data.velocity().length();

        // Get a number to multiply velocity by between 0 and 1
        double projVelocityMod = controllerVelocity > mediumThrowingVelocity ?
                Math.sin(mediumThrowingVelocity * Math.PI / 2 / 0.175) : Math.sin(controllerVelocity * Math.PI / 2 / 0.175);

        // Add an extra modifier based on how much faster we are than the throw
        double extraMod = 0;

        if (ActiveConfig.FILE_SERVER.allowThrowingBeyondVanillaMaxRange) {
            extraMod = (controllerVelocity - mediumThrowingVelocity) / 4;
            extraMod = Math.min(extraMod, 0.05);
        }

        return projVelocityMod + extraMod;
    }
}
