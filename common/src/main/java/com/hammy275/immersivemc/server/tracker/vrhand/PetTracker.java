package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PetTracker extends AbstractVRHandTracker {

    public static final double THRESHOLD = 0.02;
    private static final List<PettingHandler<?>> PETTING_HANDLERS = new ArrayList<>();

    private final Map<UUID, Integer> cooldown = new HashMap<>();

    static {
        ImmersiveMCRegistrationImpl.doPettingHandlerRegistration(PETTING_HANDLERS::add);
    }

    public void globalTick() {
        for (Map.Entry<UUID, Integer> entry : cooldown.entrySet()) {
            entry.setValue(entry.getValue() - 1);
        }
        cooldown.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRPose) {
        return VRAPI.instance().getHistoricalVRPoses(player) != null && ThreadLocalRandom.current().nextInt(20) == 0;
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRData) {
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        for (PetInfo<?> info : this.getPlayerPetsNearby((ServerPlayer) player)) {
            if (info.pet.getBoundingBox().inflate(0.2).contains(currentVRData.getHand(hand).getPos())) {
                if (history.averageSpeed(VRBodyPart.fromInteractionHand(hand), 1) >= THRESHOLD) {
                    ServerLevel level = (ServerLevel) player.level();
                    Vec3 pos = currentVRData.getHand(hand).getPos();
                    level.sendParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, ThreadLocalRandom.current().nextInt(5) + 1,
                            0.25, 0.1, 0.25, 0.00001);
                    if (!cooldown.containsKey(info.pet.getUUID())) {
                        int cooldown = info.handlePet((ServerPlayer) player, pos);
                        if (cooldown > 0) {
                            this.cooldown.put(info.pet.getUUID(), cooldown);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnabledInConfig(ActiveConfig config) {
        return config.allowPetting;
    }

    protected List<PetInfo<?>> getPlayerPetsNearby(ServerPlayer player) {
        List<PetInfo<?>> pets = new ArrayList<>();
        List<Entity> ents = player.level().getEntities(player, AABB.ofSize(player.position(), 10, 10, 10));
        for (Entity e : ents) {
            for (PettingHandler<?> handler : PETTING_HANDLERS) {
                if (handler.matchesEntity(player, e)) {
                    pets.add(PetInfo.create(e, handler));
                    break;
                }
            }
        }
        return pets;
    }

    protected record PetInfo<P extends Entity>(P pet, PettingHandler<P> handler) {

        @SuppressWarnings("unchecked")
        public static <P extends Entity> PetInfo<P> create(Entity entity, PettingHandler<P> handler) {
            return new PetInfo<>((P) entity, handler);
        }

        public int handlePet(ServerPlayer petter, Vec3 handPos) {
            return handler.doPetEffect(petter, pet, handPos);
        }
    }
}
