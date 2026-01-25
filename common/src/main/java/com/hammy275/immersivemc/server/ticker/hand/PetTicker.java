package com.hammy275.immersivemc.server.ticker.hand;

import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.ticker.AbstractHandTicker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PetTicker extends AbstractHandTicker {

    public static final double THRESHOLD = 0.02;
    private static final List<PettingHandler<?>> PETTING_HANDLERS = new ArrayList<>();
    
    static {
        ImmersiveMCRegistrationImpl.doPettingHandlerRegistration(PETTING_HANDLERS::add);
    }

    @Override
    protected void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        for (PetInfo<?> info : this.getPlayerPetsNearby((ServerPlayer) player)) {
            if (info.pet.getBoundingBox().inflate(0.2).contains(handData.getPos())) {
                if (poseHistory.averageSpeed(VRBodyPart.fromInteractionHand(hand), 1) >= THRESHOLD) {
                    ServerLevel level = (ServerLevel) player.level();
                    Vec3 pos = handData.getPos();
                    level.sendParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, ThreadLocalRandom.current().nextInt(5) + 1,
                            0.25, 0.1, 0.25, 0.00001);
                    int cooldown = info.handlePet((ServerPlayer) player, pos);
                    if (cooldown > 0) {
                        setCooldown(player, cooldown);
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        return ActiveConfig.getConfigForPlayer(player).allowPetting && ThreadLocalRandom.current().nextInt(20) == 0;
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
