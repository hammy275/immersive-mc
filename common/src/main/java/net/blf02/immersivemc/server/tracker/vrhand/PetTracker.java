package net.blf02.immersivemc.server.tracker.vrhand;

import net.blf02.immersivemc.common.config.ServerPlayerConfig;
import net.blf02.immersivemc.server.LastTickVRData;
import net.blf02.immersivemc.server.PlayerConfigs;
import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PetTracker extends AbstractVRHandTracker {

    public static final double THRESHOLD = 0.02;

    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        return this.getPlayerPetsNearby(player).size() > 0;
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        if (ThreadLocalRandom.current().nextInt(20) == 0) {
            for (LivingEntity entity : this.getPlayerPetsNearby(player)) {
                if (entity.getBoundingBox().inflate(0.2).contains(currentVRData.getController(hand.ordinal()).position())) {
                    if (LastTickVRData.getAllVelocity(lastVRData.lastPlayer.getController(hand.ordinal()),
                            currentVRData.getController(hand.ordinal()), lastVRData) >= THRESHOLD) {
                        ServerLevel level = (ServerLevel) player.level;
                        Vec3 pos = currentVRData.getController(hand.ordinal()).position();
                        level.sendParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, ThreadLocalRandom.current().nextInt(5) + 1,
                                0.25, 0.1, 0.25, 0.00001);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnabledInConfig(ServerPlayerConfig config) {
        return config.canPet;
    }

    protected List<LivingEntity> getPlayerPetsNearby(Player player) {
        List<LivingEntity> pets = new LinkedList<>();
        List<Entity> ents = player.level.getEntities(player, AABB.ofSize(player.position(), 10, 10, 10));
        for (Entity e : ents) {
            if (PlayerConfigs.getConfig(player).canPetAnyLiving) {
                if (e instanceof LivingEntity le) {
                    pets.add(le);
                }
            } else if (e instanceof TamableAnimal ta) {
                if (ta.getOwner() == player) {
                    pets.add(ta);
                }
            } else if (e instanceof AbstractHorse horse) {
                if (horse.isTamed()) {
                    pets.add(horse);
                }
            }
        }
        return pets;
    }
}
