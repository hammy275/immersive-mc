package com.hammy275.immersivemc.common.ticker;

import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.ArrayList;
import java.util.List;

public class TickerInit {

    private static final List<AbstractTicker> clientTickers = new ArrayList<>();
    private static final List<AbstractTicker> serverTickers = new ArrayList<>();

    public static void addClientTicker(AbstractTicker ticker) {
        checkForShared(ticker, clientTickers);
        clientTickers.add(ticker);
    }

    public static void addServerTicker(AbstractTicker ticker) {
        checkForShared(ticker, serverTickers);
        serverTickers.add(ticker);
    }

    public static void addCommonTicker(AbstractTicker ticker) {
        addClientTicker(ticker);
        addServerTicker(ticker);
    }

    public static void tickClient(Player player) {
        tickTickers(player, clientTickers);
    }

    public static void tickServer(Player player) {
        tickTickers(player, serverTickers);
    }

    public static void onPlayerDisconnectClient(Player player) {
        onPlayerDisconnectShared(player, clientTickers);
    }

    public static void onPlayerDisconnectServer(Player player) {
        onPlayerDisconnectShared(player, serverTickers);
    }

    private static void onPlayerDisconnectShared(Player player, List<AbstractTicker> tickers) {
        for (AbstractTicker ticker : tickers) {
            ticker.onPlayerDisconnect(player);
        }
    }

    private static void checkForShared(AbstractTicker ticker, List<AbstractTicker> toCheck) {
        for (AbstractTicker otherTicker : toCheck) {
            if (ticker == otherTicker) {
                throw new IllegalArgumentException("Can't add " + ticker + " since it's already registered!");
            }
        }
    }

    private static void tickTickers(Player player, List<AbstractTicker> tickers) {
        if (!VRVerify.playerInVR(player)) return;
        VRPose pose = VR.API.getVRPose(player);
        VRPoseHistory poseHistory = VR.API.getHistoricalVRPoses(player);
        if (pose != null && poseHistory != null) {
            for (AbstractTicker ticker : tickers) {
                ticker.doTick(player, pose, poseHistory);
            }
        }
    }
}
