package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AnvilData;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.storage.AnvilStorage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;

public class Immersives {

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> IMMERSIVES =
            new LinkedList<>();

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> WS_IMMERSIVES =
            new LinkedList<>();

    public static final BuiltImmersive immersiveAnvil = ImmersiveBuilder.create(ImmersiveCheckers::isAnvil)
            .setConfigChecker(() -> ActiveConfig.useAnvilImmersion)
            .setRenderTime(ClientConstants.ticksToRenderAnvil)
            .setRenderSize(ClientConstants.itemScaleSizeAnvil)
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(0, -1d/3d, 0), // When you place an anvil, the anvil's look direction is rotated 90 degrees.
                    ClientConstants.itemScaleSizeAnvil).upDownRenderDir(Direction.UP).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(0d, 0, 0),
                    ClientConstants.itemScaleSizeAnvil).upDownRenderDir(Direction.UP).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 1d/3d, 0),
                    ClientConstants.itemScaleSizeAnvil).holdsItems(true).upDownRenderDir(Direction.UP).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 0, 0.5), 0)
                    .textSupplier((info) -> {
                        AnvilData data = (AnvilData) info.getExtraData();
                        if (data.anvilCost == 0) return null;
                        return Component.literal(I18n.get("immersivemc.immersive.anvil.levels_needed", data.anvilCost));
                    })
                    .build())
            .setExtraStorageConsumer((storage, info) -> {
                AnvilStorage aStorage = (AnvilStorage) storage;
                AnvilData data = (AnvilData) info.getExtraData();
                data.anvilCost = aStorage.xpLevels;
            })
            .setPositioningMode(HitboxPositioningMode.TOP_BLOCK_FACING)
            .setMaxImmersives(1)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), slot, hand)))
            .setUsesWorldStorage(true)
            .setExtraInfoDataClass(AnvilData.class)
            .build();
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
            .setRightClickHandler((info, player, slot, hand) ->
                    Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand)))
            .build();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final ImmersiveHopper immersiveHopper = new ImmersiveHopper();
    public static final BuiltImmersive immersiveJukebox = ImmersiveBuilder.create(ImmersiveCheckers::isJukebox)
            .setConfigChecker(() -> ActiveConfig.useJukeboxImmersion)
            .setRenderTime(ClientConstants.ticksToHandleJukebox)
            .addHitbox(HitboxInfoBuilder.create(Vec3.ZERO, 0.125, 0.125, 0.625).build())
            .setPositioningMode(HitboxPositioningMode.TOP_LITERAL)
            .setMaxImmersives(1)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), 0, hand)))
            .setVROnly(true)
            .build();

    public static final ImmersiveRepeater immersiveRepeater = new ImmersiveRepeater();
    public static final ImmersiveShulker immersiveShulker = new ImmersiveShulker();
    public static final ImmersiveSmithingTable immersiveSmithingTable = new ImmersiveSmithingTable();

}
