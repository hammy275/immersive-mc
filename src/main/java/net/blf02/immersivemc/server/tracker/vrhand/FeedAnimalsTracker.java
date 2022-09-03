package net.blf02.immersivemc.server.tracker.vrhand;

import net.blf02.immersivemc.common.config.ServerPlayerConfig;
import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeedAnimalsTracker extends AbstractVRHandsTracker {
    public static final int COOLDOWN_TICKS = 8;

    protected Map<String, Integer> cooldown = new HashMap<>();

    @Override
    public boolean isEnabledInConfig(ServerPlayerConfig config) {
        return config.canFeedAnimals;
    }

    @Override
    protected boolean shouldRun(Player player, IVRPlayer vrPlayer, LastTickData lastVRData) {
        int currentCooldown = cooldown.getOrDefault(player.getGameProfile().getName(), 0);
        if (currentCooldown > 0) {
            cooldown.put(player.getGameProfile().getName(), --currentCooldown);
        }
        return (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
                && currentCooldown <= 0;
    }

    @Override
    protected void run(Player player, IVRPlayer vrPlayer, LastTickData lastVRData) {
        InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack inHand = player.getItemInHand(hand);
        if (inHand.isEmpty()) return;
        List<Animal> nearbyEnts = getLivingNearby(player);
        for (Animal animal : nearbyEnts) {
            AABB feedbox = getMouthHitbox(animal);

            if (feedbox.contains(vrPlayer.getController0().position()) && feedbox.contains(vrPlayer.getController1().position())) {
                InteractionResult res = animal.mobInteract(player, hand);
                if (res == InteractionResult.CONSUME || res == InteractionResult.SUCCESS) {
                    cooldown.put(player.getGameProfile().getName(), COOLDOWN_TICKS);
                    break;
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
            well with other mods. So instead, I'm putting a box around the eyes lol.

            For now, this uses the entire entity's hitbox, because entity.getLookAngle() is completely breaking down
            for one reason or another. TODO: Find a fix for this
         */
        return entity.getBoundingBox().inflate(0.4);
    }

    public static List<Animal> getLivingNearby(Player player) {
        AABB checkArea = AABB.ofSize(player.position(), 6, 6, 6);
        List<Entity> ents = player.level.getEntities(player, checkArea, (e) -> e instanceof Animal);
        List<Animal> nearby = new LinkedList<>();
        for (Entity e : ents) {
            nearby.add((Animal) e);
        }
        return nearby;

    }
}
