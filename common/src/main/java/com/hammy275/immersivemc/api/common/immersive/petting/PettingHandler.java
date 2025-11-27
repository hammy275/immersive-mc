package com.hammy275.immersivemc.api.common.immersive.petting;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * An object that defines how an entity should handle being pet. Can be registered in the {@link Consumer} provided to
 * {@link com.hammy275.immersivemc.api.common.ImmersiveMCRegistration#addPettingHandlerRegistrationHandler(Consumer)}.
 * Note that the actual pet detection is handled by ImmersiveMC, only the methods provided here need implementation.
 * @param <P> The type of entity that should handle being pet.
 * @since 1.6.0-alpha1
 */
public interface PettingHandler<P extends Entity> {

    /**
     * Whether the entity provided matches being pet by this handler. Any entity that matches here will not be
     * handled by handlers that were registered after this one.
     * @param playerPetting The player that is petting the potential pet.
     * @param possibleEntityToPet The entity that may be a pet for this handler.
     * @return Whether the entity provided has petting handled by this handler.
     */
    public boolean matchesEntity(ServerPlayer playerPetting, Entity possibleEntityToPet);

    /**
     * Perform effects from an entity being pet. This happens randomly as determined by ImmersiveMC, not every tick or
     * some other consistent metric! Note that displaying hearts is automatically performed by ImmersiveMC and does not
     * need to be replicated here.
     *
     * @param playerPetting The player performing the petting.
     * @param petEntity The entity being pet.
     * @param handPosition The position of the hand performing the petting.
     * @return A cooldown to wait in ticks until the entity pet effect can trigger again. Note that ImmersiveMC's hearts
     * can still trigger on the next tick. A value of 0 or less is treated as no cooldown.
     */
    public int doPetEffect(ServerPlayer playerPetting, P petEntity, Vec3 handPosition);
}
