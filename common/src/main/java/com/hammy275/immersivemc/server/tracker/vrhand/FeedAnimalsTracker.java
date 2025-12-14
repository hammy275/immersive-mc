package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.vivecraft.api.data.VRPose;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeedAnimalsTracker extends AbstractVRHandsTracker {
    public static final int COOLDOWN_TICKS = 20;

    protected Map<String, Integer> cooldown = new HashMap<>();

    @Override
    public boolean isEnabledInConfig(ActiveConfig config) {
        return config.useFeedingAnimalsImmersive;
    }

    @Override
    protected boolean shouldRun(Player player, VRPose vrPose) {
        int currentCooldown = cooldown.getOrDefault(player.getGameProfile().name(), 0);
        if (currentCooldown > 0) {
            cooldown.put(player.getGameProfile().name(), --currentCooldown);
        }
        return (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
                && currentCooldown <= 0;
    }

    @Override
    protected void run(Player player, VRPose vrPose) {
        InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack inHand = player.getItemInHand(hand);
        if (inHand.isEmpty()) return;
        List<Animal> nearbyEnts = getLivingNearby(player);
        for (Animal animal : nearbyEnts) {
            if (animal.isFood(inHand)) {
                AABB feedbox = getMouthHitbox(animal);
                if (feedbox.contains(vrPose.getMainHand().getPos()) && feedbox.contains(vrPose.getOffHand().getPos())
                && vrPose.getMainHand().getPos().distanceToSqr(vrPose.getOffHand().getPos()) < 0.5) {
                    InteractionResult res = animal.mobInteract(player, hand);
                    if (res == InteractionResult.CONSUME || res == InteractionResult.SUCCESS) {
                        cooldown.put(player.getGameProfile().name(), COOLDOWN_TICKS);
                        VRRumble.doubleRumbleIfVR(player, CommonConstants.vibrationTimePlayerActionAlert);
                        break;
                    }
                }
            }
        }
    }

    public static AABB getMouthHitbox(LivingEntity entity) {
        /*
            There's no really good way to get the position of the mouth hitbox, sadly.

            You'd think to find the eye position and go a bit below, which is what I tried originally, but cats
            have their eyes literally where their neck is, while dogs have it where their eyes are. So we can't
            really do that, and I'm not putting in exceptions (at least not yet), since I want this to work pretty
            well with other mods.

            On top of the above, eyes appear to actually originate from the center of the mob, not the front, so we need
            to draw a giant hitbox width-wise. On top of THAT, due to how "long" cats and dogs are, we need to increase
            the size of the hitbox width-wise to cover the front of them. All of this said, we go to the eye position,
            and draw a HUGE box for this.
         */
        return AABB.ofSize(entity.getEyePosition(), entity.getBbWidth() * 2.25,
                entity.getBbHeight() * 0.5,
                entity.getBbWidth() * 2.25);
    }

    public static List<Animal> getLivingNearby(Player player) {
        AABB checkArea = AABB.ofSize(player.position(), 6, 6, 6);
        List<Entity> ents = player.level().getEntities(player, checkArea, (e) -> e instanceof Animal);
        List<Animal> nearby = new LinkedList<>();
        for (Entity e : ents) {
            nearby.add((Animal) e);
        }
        return nearby;

    }
}
