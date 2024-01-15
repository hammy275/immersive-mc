package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AnvilData;
import com.hammy275.immersivemc.client.immersive.info.ChestLikeData;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.storage.AnvilStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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
                    ClientConstants.itemScaleSizeAnvil).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(0d, 0, 0),
                    ClientConstants.itemScaleSizeAnvil).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 1d/3d, 0),
                    ClientConstants.itemScaleSizeAnvil).holdsItems(true).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 0, 0.5), 0)
                    .textSupplier((info) -> {
                        AnvilData data = (AnvilData) info.getExtraData();
                        if (data.anvilCost == 0) return null;
                        return List.of(new Pair<>(Component.literal(I18n.get("immersivemc.immersive.anvil.levels_needed", data.anvilCost)), Vec3.ZERO));
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
    public static final BuiltImmersive immersiveBarrel = ImmersiveBuilder.create(ImmersiveCheckers::isBarrel)
            .setConfigChecker(() -> ActiveConfig.useBarrelImmersion)
            .setRenderTime(ClientConstants.ticksToRenderBarrel)
            .setRenderSize(ClientConstants.itemScaleSizeBarrel)
            .add3x3Grid(HitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(HitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(HitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0.25, 1d/16d, 0.15), 0.35, 0.35, 0.5)
                    .setVRMovementInfo(new HitboxVRMovementInfo(Direction.Axis.Z, new double[]{0.05},
                            HitboxVRMovementInfo.ControllerMode.EITHER, (info) -> {
                        ChestLikeData extra = (ChestLikeData) info.getExtraData();
                        extra.toggleOpen(info.getBlockPosition());
                    }))
                    .build())
            .setPositioningMode(HitboxPositioningMode.BLOCK_FACING_NEG_X)
            .setMaxImmersives(4)
            .setRightClickHandler((info, player, slot, hand) -> {
                if (slot < 27) {
                    Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                }
            })
            .setExtraInfoDataClass(ChestLikeData.class)
            .setSlotActiveFunction((info, slot) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return (slot < 27 && extra.isOpen && slot >= extra.currentRow * 9 && slot < (extra.currentRow + 1) * 9)
                        || (slot == 27 && !extra.isOpen);
            })
            .setOnRemove((info) -> {
                ((ChestLikeData) info.getExtraData()).forceClose(info.getBlockPosition());
            })
            .build();
    public static final ImmersiveBeacon immersiveBeacon = new ImmersiveBeacon();
    public static final BuiltImmersive immersiveBrewing = ImmersiveBuilder.create(ImmersiveCheckers::isBrewingStand)
            .setConfigChecker(() -> ActiveConfig.useBrewingImmersion)
            .setRenderTime(ClientConstants.ticksToRenderBrewing)
            .setRenderSize(ClientConstants.itemScaleSizeBrewing)
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(-0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.autoCenterBrewing ? -1d/6d : -0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.autoCenterBrewing ? 0.1 : 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> ActiveConfig.autoCenterBrewing ? new Vec3(0, 0.35, 0) : new Vec3(-0.25, 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setMaxImmersives(2)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand)))
            .build();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final BuiltImmersive immersiveCrafting = ImmersiveBuilder.create(ImmersiveCheckers::isCraftingTable)
            .setConfigChecker(() -> ActiveConfig.useCraftingImmersion)
            .setRenderTime(ClientConstants.ticksToRenderCrafting)
            .setRenderSize(ClientConstants.itemScaleSizeCrafting)
            .add3x3Grid(HitboxInfoBuilder.createItemInput(Vec3.ZERO,
                            ClientConstants.itemScaleSizeCrafting / 1.5f).needs3DResourcePackCompat(true).build(),
                    3d / 16d)
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 0, 0.5),
                    ClientConstants.itemScaleSizeCrafting * 1.5d).holdsItems(true)
                    .itemSpins(true).itemRenderSizeMultiplier(3f).triggerHitbox(true)
                    .forceUpDownRenderDir(ForcedUpDownRenderDir.NULL).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setMaxImmersives(1)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), slot, hand)))
            .setUsesWorldStorage(true)
            .setTriggerHitboxControllerNum(0)
            .build();
    public static final BuiltImmersive immersiveETable = ImmersiveBuilder.create(ImmersiveCheckers::isEnchantingTable)
            .setConfigChecker(() -> ActiveConfig.useETableImmersion)
            .setRenderTime(ClientConstants.ticksToRenderETable)
            .setRenderSize(ClientConstants.itemScaleSizeETable)
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(0, 0.75, -0.5), ClientConstants.itemScaleSizeETable).build())
            .addHitbox(HitboxInfoBuilder.create((info) -> {
                int yOffset = info.ticksActive % ClientConstants.eTableYOffsets.size();
                return new Vec3(-0.5, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).weakData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (info.slotHovered != 1) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (" + 1 + ")"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.itemHitboxes.get(0).item != null && !info.itemHitboxes.get(0).item.isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(HitboxInfoBuilder.create((info) -> {
                int yOffset = (info.ticksActive + 7) % ClientConstants.eTableYOffsets.size();
                return new Vec3(0, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).midData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (info.slotHovered != 2) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (" + 1 + ")"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.itemHitboxes.get(0).item != null && !info.itemHitboxes.get(0).item.isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(HitboxInfoBuilder.create((info) -> {
                int yOffset = (info.ticksActive + 14) % ClientConstants.eTableYOffsets.size();
                return new Vec3(0.5, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).strongData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (info.slotHovered != 3) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (" + 1 + ")"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.itemHitboxes.get(0).item != null && !info.itemHitboxes.get(0).item.isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setMaxImmersives(1)
            .setUsesWorldStorage(true)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), slot, hand)))
            .setExtraInfoDataClass(EnchantingData.class)
            .setExtraStorageConsumer((storage, info) -> {
                ItemStack item = info.itemHitboxes.get(0).item;
                if (item != null && !item.isEmpty()) {
                    if (item.getItem() == Items.BOOK) {
                        item = new ItemStack(Items.ENCHANTED_BOOK);
                    } else {
                        item = item.copy();
                    }
                    EnchantmentHelper.setEnchantments(ClientConstants.fakeEnch, item);
                } else {
                    item = ItemStack.EMPTY;
                }
                for (int i = 1; i <= 3; i++) {
                    info.itemHitboxes.get(i).item = item;
                }
            })
            .build();
    public static final BuiltImmersive immersiveFurnace = ImmersiveBuilder.create(ImmersiveCheckers::isFurnace)
            .setConfigChecker(() -> ActiveConfig.useFurnaceImmersion)
            .setRenderTime(ClientConstants.ticksToRenderFurnace)
            .setRenderSize(ClientConstants.itemScaleSizeFurnace)
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                if (ActiveConfig.autoCenterFurnace) {
                    if (info.itemHitboxes.get(2).item == null || info.itemHitboxes.get(2).item.isEmpty()) {
                        return new Vec3(0, 0.25, 0);
                    } else if (info.itemHitboxes.get(0).item == null || info.itemHitboxes.get(0).item.isEmpty()) {
                        return null;
                    } else {
                        return new Vec3(-0.25, 0.25, 0);
                    }
                } else {
                    return new Vec3(-0.25, 0.25, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).needs3DResourcePackCompat(true).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                if (ActiveConfig.autoCenterFurnace) {
                    return new Vec3(0, -0.25, 0);
                } else {
                    return new Vec3(-0.25, -0.25, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).needs3DResourcePackCompat(true).build())
            .addHitbox(HitboxInfoBuilder.create((info) -> {
                if (ActiveConfig.autoCenterFurnace) {
                    if (info.itemHitboxes.get(2).item == null || info.itemHitboxes.get(2).item.isEmpty()) {
                        return null;
                    } else if (info.itemHitboxes.get(0).item == null || info.itemHitboxes.get(0).item.isEmpty()) {
                        return new Vec3(0, 0.25, 0);
                    } else {
                        return new Vec3(0.25, 0.25, 0);
                    }
                } else {
                    return new Vec3(0.25, 0, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).holdsItems(true).needs3DResourcePackCompat(true).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING)
            .setMaxImmersives(4)
            .setRightClickHandler((info, player, slot, hand) -> {
                if (ActiveConfig.autoCenterFurnace) {
                    if (info.itemHitboxes.get(0).getPos() == null && slot == 2) {
                        ItemStack handItem = player.getItemInHand(hand);
                        if (!handItem.isEmpty() &&
                                (!Util.stacksEqualBesidesCount(handItem, info.itemHitboxes.get(2).item) || handItem.getCount() == handItem.getMaxStackSize())) {
                            // If we don't have an input slot, set to the input slot instead of output if:
                            // Our hand is NOT empty (we have something to put in) AND
                            // We're holding a different item than what's in the output OR what we have in our hand can't be added to
                            slot = 0;
                        }
                    }
                }
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
            })
            .build();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final BuiltImmersive immersiveHopper = ImmersiveBuilder.create(ImmersiveCheckers::isHopper)
            .setConfigChecker(() -> ActiveConfig.useHopperImmersion)
            .setRenderTime(ClientConstants.ticksToRenderHopper)
            .setRenderSize(ClientConstants.itemScaleSizeHopper)
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, 3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * -2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * -1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(0, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * 1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(HitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, -3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * 2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).build())
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_NO_DOWN)
            .setMaxImmersives(2)
            .setRightClickHandler((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand)))
            .build();
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
    public static final BuiltImmersive immersiveShulker = ImmersiveBuilder.create(ImmersiveCheckers::isShulkerBox)
            .setConfigChecker(() -> ActiveConfig.useShulkerImmersion)
            .setRenderTime(ClientConstants.ticksToRenderShulker)
            .setRenderSize(ClientConstants.itemScaleSizeShulker)
            .add3x3Grid(HitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0, -1d/3d * extra.offsetIn(0));
            }, 0.14f).build(), 0.15)
            .add3x3Grid(HitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0, -1d/3d * extra.offsetIn(1));
            }, 0.14f).build(), 0.15)
            .add3x3Grid(HitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0, -1d/3d * extra.offsetIn(2));
            }, 0.14f).build(), 0.15)
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING)
            .setMaxImmersives(4)
            .setRightClickHandler(((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand))))
            .setExtraInfoDataClass(ChestLikeData.class)
            .setSlotActiveFunction((info, slot) -> ((ChestLikeData) info.getExtraData()).isOpen)
            .setOnRemove((info) -> ((ChestLikeData) info.getExtraData()).forceClose(info.getBlockPosition()))
            .setShouldRenderItemGuideFunction((info, slot) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return slot >= extra.currentRow * 9 && slot < (extra.currentRow + 1) * 9;
            })
            .build();

    public static final BuiltImmersive immersiveSmithingTable = ImmersiveBuilder.create(ImmersiveCheckers::isSmithingTable)
            .setConfigChecker(() -> ActiveConfig.useSmithingTableImmersion)
            .setRenderTime(ClientConstants.ticksToRenderSmithingTable)
            .setRenderSize(ClientConstants.itemScaleSizeSmithingTable)
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(-1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(Vec3.ZERO, ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(HitboxInfoBuilder.createItemInput(new Vec3(1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(HitboxInfoBuilder.create(new Vec3(0, 0, 0.5), ClientConstants.itemScaleSizeSmithingTable / 1.025).holdsItems(true).triggerHitbox(true).itemSpins(true).itemRenderSizeMultiplier(1.5f).forceUpDownRenderDir(ForcedUpDownRenderDir.NULL).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setMaxImmersives(1)
            .setRightClickHandler(((info, player, slot, hand) -> Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), slot, hand))))
            .setUsesWorldStorage(true)
            .setTriggerHitboxControllerNum(0)
            .build();


    public static final BuiltImmersive immersiveIronFurnacesFurnace = immersiveFurnace.getBuilderClone()
            .setBlockChecker(ImmersiveCheckers::isIronFurnacesFurnace)
            .setConfigChecker(() -> ActiveConfig.useIronFurnacesFurnaceImmersion)
            .build();
}
