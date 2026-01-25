package com.hammy275.immersivemc.common.ticker;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ticker system to perform something each tick. Only runs for VR players!
 */
public abstract class AbstractTicker {

    /**
     * Map of all cooldowns. Players on cooldown do not call {@link #shouldTick} or {@link #tick}.
     */
    private Map<UUID, Integer> cooldowns = new HashMap<>();

    /**
     * Tick method that performs some action (usually). Called for each player on each tick when the player is not
     * on cooldown and {@link #shouldTick} returns true.
     * @param player The player running for this ticker.
     * @param pose The player's pose in VR.
     * @param poseHistory The player's pose history in VR.
     */
    protected abstract void tick(Player player, VRPose pose, VRPoseHistory poseHistory);

    /**
     * Method that checks if the action should be performed (usually). Called for each player on each tick when the
     * player is not on cooldown.
     * @param player The player running for this ticker.
     * @param pose The player's pose in VR.
     * @param poseHistory The player's pose history in VR.
     * @return Whether the {@link #tick} method should be run for the player.
     */
    protected abstract boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory);

    /**
     * Cleanup to run on player disconnect. Can be overridden to perform additional cleanup, but should always super
     * call to clean up cooldown info.
     * @param player The player disconnecting. Note that the player may not be in VR, and Vivecraft may not even
     *               be installed!
     */
    public void onPlayerDisconnect(Player player) {
        cooldowns.remove(player.getUUID());
    }

    /**
     * Entrypoint for actually running tickers. Should be called on each tick to actually run ticker logic.
     * @param player The player running for this ticker.
     * @param pose The player's pose in VR.
     * @param poseHistory The player's pose history in VR.
     */
    public final void doTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        // Cooldown management
        Integer cooldown = cooldowns.get(player.getUUID());
        if (cooldown != null) {
            // Only put new value if not hitting 0 (or lower). If hitting 0 (or lower), just remove it.
            if (cooldown > 1) {
                cooldowns.put(player.getUUID(), cooldown - 1);
            } else {
                cooldowns.remove(player.getUUID());
            }
        } else if (shouldTick(player, pose, poseHistory)) {
            tick(player, pose, poseHistory);
        }
    }

    /**
     * Set the cooldown for a player for this ticker, thus preventing this ticker from running for them until the
     * cooldown expires.
     * @param player Player to set a cooldown for.
     * @param cooldown The cooldown in ticks until this ticker should be able to run again. Note that a cooldown of
     *                 0 or below is ignored.
     */
    protected final void setCooldown(Player player, int cooldown) {
        if (cooldown > 0) {
            cooldowns.put(player.getUUID(), cooldown);
        }
    }
}
