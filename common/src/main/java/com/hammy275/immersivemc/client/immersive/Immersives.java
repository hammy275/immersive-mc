package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltHorizontalBlockInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;

public class Immersives {

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> IMMERSIVES =
            new LinkedList<>();

    public static final List<AbstractWorldStorageImmersive<? extends AbstractWorldStorageInfo>> WS_IMMERSIVES =
            new LinkedList<>();

    public static final ImmersiveAnvil immersiveAnvil = new ImmersiveAnvil();
    public static final ImmersiveBackpack immersiveBackpack = new ImmersiveBackpack();
    public static final ImmersiveBarrel immersiveBarrel = new ImmersiveBarrel();
    public static final ImmersiveBeacon immersiveBeacon = new ImmersiveBeacon();
    public static final ImmersiveBrewing immersiveBrewing = new ImmersiveBrewing();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final ImmersiveChiseledBookshelf immersiveChiseledBookshelf = new ImmersiveChiseledBookshelf();
    public static final ImmersiveCrafting immersiveCrafting = new ImmersiveCrafting();
    public static final ImmersiveETable immersiveETable = new ImmersiveETable();
    public static final BuiltImmersive immersiveFurnace = ImmersiveBuilder.create(ImmersiveCheckers::isFurnace)
            .setConfigChecker(() -> ActiveConfig.useFurnaceImmersion)
            .setRenderTime(ClientConstants.ticksToRenderFurnace)
            .setRenderSize(ClientConstants.itemScaleSizeFurnace)
            .addItemHitbox(new Vec3(-0.25, 0.25, 0), ClientConstants.itemScaleSizeFurnace / 1.5d, true, true)
            .addItemHitbox(new Vec3(-0.25, -0.25, 0), ClientConstants.itemScaleSizeFurnace / 1.5d, true, true)
            .addItemHitbox(new Vec3(0.25, 0, 0), ClientConstants.itemScaleSizeFurnace / 1.5d, false, true)
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING)
            .setMaxImmersives(4)
            .setExtraRenderReady((info) -> {
                BuiltHorizontalBlockInfo horizInfo = (BuiltHorizontalBlockInfo) info;
                BlockState forwardBlock = Minecraft.getInstance().level.getBlockState(info.getBlockPosition().relative(horizInfo.dir));
                return forwardBlock.isAir();
            })
            .setRightClickHandler((info, player, slot, hand) ->
                    Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand)))
            .build();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final ImmersiveHopper immersiveHopper = new ImmersiveHopper();
    public static final ImmersiveJukebox immersiveJukebox = new ImmersiveJukebox();
    public static final ImmersiveRepeater immersiveRepeater = new ImmersiveRepeater();
    public static final ImmersiveShulker immersiveShulker = new ImmersiveShulker();
    public static final ImmersiveSmithingTable immersiveSmithingTable = new ImmersiveSmithingTable();

}
