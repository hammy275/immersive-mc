package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;

public class LastClientVRData {

    private static final LinkedList<IVRPlayer> lastPlayers = new LinkedList<>();
    private static final LinkedList<Vec3> lastPositions = new LinkedList<>();

    /**
     * Adds new last tick entry. Shouldn't be called by anything other than LastVRDataTracker.
     * @param player IVRPlayer to add.
     */
    public static void addLastTick(IVRPlayer player) {
        lastPlayers.addFirst(player);
        if (lastPlayers.size() > 5) {
            lastPlayers.removeLast();
        }

        if (Minecraft.getInstance().player != null) {
            lastPositions.addFirst(ClientUtil.playerPos());
            if (lastPositions.size() > 5) {
                lastPositions.removeLast();
            }
        }


    }

    /**
     * @param ticksAgo Ticks ago to check back.
     * @return Whether we have data ticksAgo ticks back.
     */
    public static boolean hasPlayerTicksAgo(int ticksAgo) {
        return lastPlayers.size() >= ticksAgo;
    }

    /**
     * Get IVRPlayer from some ticks ago.
     * @param ticksAgo Ticks ago to retrieve. 0 retrieves the current tick and >5 throws as we only keep the past 5 ticks.
     * @return The IVRPlayer for ticksAgo ticks ago.
     */
    public static IVRPlayer getPlayer(int ticksAgo) {
        if (ticksAgo > 5) {
            throw new IllegalArgumentException("Only holds data for past 5 ticks");
        } else if (ticksAgo == 0) {
            return VRPlugin.API.getVRPlayer(Minecraft.getInstance().player); // If ticksAgo == 0, use current tick
        }
        return lastPlayers.get(ticksAgo - 1);
    }

    /**
     * @return Whether changeForVelocity can be run.
     */
    public static boolean canGetVelocityChange() {
        return hasPlayerTicksAgo(2);
    }

    /**
     * Gets the change in position for velocity purposes
     * @return Vector3d representing x, y, and z velocity
     */
    public static Vec3 changeForVelocity(VRType type) {
        IVRData last = type == VRType.HMD ? getPlayer(2).getHMD() : getPlayer(2).getController(type.index);
        IVRData current = type == VRType.HMD ? VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getHMD() :
                VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(type.index);

        Vec3 lastPlayerPos = lastPositions.get(1);
        Vec3 currentPlayerPos = ClientUtil.playerPos();
        Vec3 posDiff = currentPlayerPos.subtract(lastPlayerPos);

        return current.position().subtract(last.position()).subtract(posDiff);
    }

    public enum VRType {
        HMD(-1), C0(0), C1(1);

        public int index;

        VRType(int index) {
            this.index = index;
        }
    }
}
