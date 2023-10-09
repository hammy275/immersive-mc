package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BuiltHorizontalBlockInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;

public class Immersives {

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> IMMERSIVES =
            new LinkedList<>();

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> WS_IMMERSIVES =
            new LinkedList<>();

    public static final ImmersiveAnvil immersiveAnvil = new ImmersiveAnvil();
    public static final ImmersiveBackpack immersiveBackpack = new ImmersiveBackpack();
    public static final ImmersiveBarrel immersiveBarrel = new ImmersiveBarrel();
    public static final ImmersiveBeacon immersiveBeacon = new ImmersiveBeacon();
    public static final ImmersiveBrewing immersiveBrewing = new ImmersiveBrewing();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final ImmersiveChiseledBookshelf immersiveChiseledBookshelf = new ImmersiveChiseledBookshelf();
    public static final BuiltImmersive immersiveCrafting = ImmersiveBuilder.create(ImmersiveCheckers::isCraftingTable)
            .setConfigChecker(() -> ActiveConfig.useCraftingImmersion)
            .setRenderTime(ClientConstants.ticksToRenderCrafting)
            .setRenderSize(ClientConstants.itemScaleSizeCrafting)
            .add3x3HorizontalGrid(HitboxInfoBuilder.createItemInput(Vec3.ZERO,
                            ClientConstants.itemScaleSizeCrafting / 1.5f).upDownRenderDir(Direction.UP).build(),
                    3d / 16d)
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 0, 0.5),
                    ClientConstants.itemScaleSizeCrafting * 1.5d).holdsItems(true)
                    .itemSpins(true).itemRenderSizeMultiplier(3f).triggerHitbox(true).build())
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING)
            .setMaxImmersives(1)
            .setExtraRenderReady((info) -> Minecraft.getInstance().level.getBlockState(info.getBlockPosition().above()).isAir())
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), slot, hand)))
            .setUsesWorldStorage(true)
            .setTriggerHitboxControllerNum(0)
            .build();
    public static final ImmersiveETable immersiveETable = new ImmersiveETable();
    public static final BuiltImmersive immersiveFurnace = ImmersiveBuilder.create(ImmersiveCheckers::isFurnace)
            .setConfigChecker(() -> ActiveConfig.useFurnaceImmersion)
            .setRenderTime(ClientConstants.ticksToRenderFurnace)
            .setRenderSize(ClientConstants.itemScaleSizeFurnace)
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(-0.25, 0.25, 0),
                    ClientConstants.itemScaleSizeFurnace / 1.5d).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(-0.25, -0.25, 0),
                    ClientConstants.itemScaleSizeFurnace / 1.5d).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0.25, 0, 0),
                    ClientConstants.itemScaleSizeFurnace / 1.5d).holdsItems(true).build())
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
