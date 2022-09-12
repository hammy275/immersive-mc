package net.blf02.immersivemc.server.tracker.vrhand;

import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Hacked extends of AbstractVRHandTracker that's intended to be used for both hands instead of one
 */
public abstract class AbstractVRHandsTracker extends AbstractVRHandTracker {
    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        return false; // NO-OP
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        // NO-OP
    }

    protected abstract boolean shouldRun(Player player, IVRPlayer vrPlayer, LastTickData lastVRData);

    protected abstract void run(Player player, IVRPlayer vrPlayer, LastTickData lastVRData);

    @Override
    public void tick(Player player, IVRPlayer vrPlayer, LastTickData lastVRData) {
        if (shouldRun(player, vrPlayer, lastVRData)) {
            run(player, vrPlayer, lastVRData);
        }
    }
}
