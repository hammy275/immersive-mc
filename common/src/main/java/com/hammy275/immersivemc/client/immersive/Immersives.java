package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.*;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AnvilData;
import com.hammy275.immersivemc.client.immersive.info.ChestLikeData;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
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
import java.util.List;

public class Immersives {

    public static final List<Immersive<? extends ImmersiveInfo, ? extends NetworkStorage>> IMMERSIVES =
            new ArrayList<>();
    public static final List<AbstractPlayerAttachmentImmersive<? extends AbstractImmersiveInfo, ? extends NetworkStorage>> IMMERSIVE_ATTACHMENTS =
            new ArrayList<>();

    public static final BuiltImmersive<?,?> immersiveAnvil = ImmersiveBuilder.create(ImmersiveHandlers.anvilHandler, AnvilData.class)
            .setConfigChecker(() -> ActiveConfig.active().useAnvilImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeAnvil)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0, -1d/3d, 0), // When you place an anvil, the anvil's look direction is rotated 90 degrees.
                    ClientConstants.itemScaleSizeAnvil).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0d, 0, 0),
                    ClientConstants.itemScaleSizeAnvil).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0, 1d/3d, 0),
                    ClientConstants.itemScaleSizeAnvil).holdsItems(true).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0, 0, 0.5), 0)
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
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();
    public static final ImmersiveBackpack immersiveBackpack = new ImmersiveBackpack();
    public static final BuiltImmersive<ChestLikeData,?> immersiveBarrel = ImmersiveBuilder.create(ImmersiveHandlers.barrelHandler, ChestLikeData.class)
            .setConfigChecker(() -> ActiveConfig.active().useBarrelImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeBarrel)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.25, 1d/16d, 0.15), 0.35, 0.35, 0.5)
                    .setVRMovementInfo(new HitboxVRMovementInfo(Direction.Axis.Z, new double[]{0.05},
                            HitboxVRMovementInfo.ControllerMode.EITHER, (info) -> {
                        ChestLikeData extra = (ChestLikeData) info.getExtraData();
                        extra.toggleOpen(info.getBlockPosition());
                    }))
                    .build())
            .setPositioningMode(HitboxPositioningMode.BLOCK_FACING_NEG_X)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                if (slot < 27) {
                    Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                    return ClientConstants.defaultCooldownTicks;
                }
                return -1;
            })
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
    public static final BuiltImmersive<?,?> immersiveBrewing = ImmersiveBuilder.create(ImmersiveHandlers.brewingStandHandler)
            .setConfigChecker(() -> ActiveConfig.active().useBrewingImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeBrewing)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(-0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.active().autoCenterBrewing ? -1d/6d : -0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.active().autoCenterBrewing ? 0.1 : 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> ActiveConfig.active().autoCenterBrewing ? new Vec3(0, 0.35, 0) : new Vec3(-0.25, 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final BuiltImmersive<?,?> immersiveChiseledBookshelf = ImmersiveBuilder.create(ImmersiveHandlers.chiseledBookshelfHandler)
            .setConfigChecker(() -> ActiveConfig.active().useChiseledBookshelfImmersion)
            .shouldDisableRightClicksWhenInteractionsDisabled(false)
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(-0.3125, 0.25, 0), 0.375, 0.5, 0.25).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.03125, 0.25, 0), 0.3125, 0.5, 0.25).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.34375, 0.25, 0), 0.3125, 0.5, 0.25).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(-0.3125, -0.25, 0), 0.375, 0.5, 0.25).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.03125, -0.25, 0), 0.3125, 0.5, 0.25).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.34375, -0.25, 0), 0.3125, 0.5, 0.25).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .setVROnly(true)
            .build();
    public static final BuiltImmersive<?,?> immersiveCrafting = ImmersiveBuilder.create(ImmersiveHandlers.craftingHandler)
            .setConfigChecker(() -> ActiveConfig.active().useCraftingImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeCrafting)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO,
                            ClientConstants.itemScaleSizeCrafting / 1.5f).needs3DResourcePackCompat(true).build(),
                    3d / 16d)
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0, 0, 0.5),
                    ClientConstants.itemScaleSizeCrafting * 1.5d).holdsItems(true)
                    .itemSpins(true).itemRenderSizeMultiplier(3f).triggerHitbox(true)
                    .forceUpDownRenderDir(ForcedUpDownRenderDir.NULL).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();
    public static final BuiltImmersive<?,?> immersiveETable = ImmersiveBuilder.create(ImmersiveHandlers.enchantingTableHandler, EnchantingData.class)
            .setConfigChecker(() -> ActiveConfig.active().useETableImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeETable)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0, 0.75, -0.5), ClientConstants.itemScaleSizeETable).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                int yOffset = (int) (info.ticksExisted() % ClientConstants.eTableYOffsets.size());
                return new Vec3(-0.5, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).weakData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(1)) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (1)"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                int yOffset = (int) ((info.ticksExisted() + 7) % ClientConstants.eTableYOffsets.size());
                return new Vec3(0, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).midData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(2)) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (2)"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                int yOffset = (int) ((info.ticksExisted() + 14) % ClientConstants.eTableYOffsets.size());
                return new Vec3(0.5, 1.25, -0.5).add(0, ClientConstants.eTableYOffsets.get(yOffset), 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).strongData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(3)) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(Component.literal(data.levelsNeeded + " (3)"), new Vec3(0, 0.33, 0)));
                    texts.add(new Pair<>(data.textPreview, new Vec3(0, -0.33, 0)));
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(Component.translatable("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .setExtraStorageConsumer((storageIn, info) -> {
                EnchantingData extraData = (EnchantingData) info.getExtraData();
                ETableStorage storage = (ETableStorage) storageIn;
                extraData.weakData.set(storage.xpLevels[0], storage.enchantHints[0], storage.levelHints[0]);
                extraData.midData.set(storage.xpLevels[1], storage.enchantHints[1], storage.levelHints[1]);
                extraData.strongData.set(storage.xpLevels[2], storage.enchantHints[2], storage.levelHints[2]);
                for (int i = 1; i <= 3; i++) {
                    EnchantingData.ETableData data = i == 1 ? extraData.weakData : i == 2 ? extraData.midData : extraData.strongData;
                    ItemStack item = info.getItem(0);
                    if (item != null && !item.isEmpty()) {
                        item = item.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : item.copy();
                        if (data.isPresent()) {
                            EnchantmentHelper.setEnchantments(ClientConstants.fakeEnch, item);
                        }
                    }
                    info.setFakeItem(i, item);
                }
            })
            .build();
    public static final BuiltImmersive<?,?> immersiveFurnace = ImmersiveBuilder.create(ImmersiveHandlers.furnaceHandler)
            .setConfigChecker(() -> ActiveConfig.active().useFurnaceImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeFurnace)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                if (ActiveConfig.active().autoCenterFurnace) {
                    if (info.getItem(2) == null || info.getItem(2).isEmpty()) {
                        return new Vec3(0, 0.25, 0);
                    } else if (info.getItem(0) == null || info.getItem(0).isEmpty()) {
                        return null;
                    } else {
                        return new Vec3(-0.25, 0.25, 0);
                    }
                } else {
                    return new Vec3(-0.25, 0.25, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).needs3DResourcePackCompat(true).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                if (ActiveConfig.active().autoCenterFurnace) {
                    return new Vec3(0, -0.25, 0);
                } else {
                    return new Vec3(-0.25, -0.25, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).needs3DResourcePackCompat(true).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                if (ActiveConfig.active().autoCenterFurnace) {
                    if (info.getItem(2) == null || info.getItem(2).isEmpty()) {
                        return null;
                    } else if (info.getItem(0) == null || info.getItem(0).isEmpty()) {
                        return new Vec3(0, 0.25, 0);
                    } else {
                        return new Vec3(0.25, 0.25, 0);
                    }
                } else {
                    return new Vec3(0.25, 0, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).holdsItems(true).needs3DResourcePackCompat(true).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                if (ActiveConfig.active().autoCenterFurnace) {
                    if (info.getAllHitboxes().get(0).getHitbox() == null && slot == 2) {
                        ItemStack handItem = player.getItemInHand(hand);
                        if (!handItem.isEmpty() &&
                                (!Util.stacksEqualBesidesCount(handItem, info.getItem(2)) || handItem.getCount() == handItem.getMaxStackSize())) {
                            // If we don't have an input slot, set to the input slot instead of output if:
                            // Our hand is NOT empty (we have something to put in) AND
                            // We're holding a different item than what's in the output OR what we have in our hand can't be added to
                            slot = 0;
                        }
                    }
                }
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final BuiltImmersive<?,?> immersiveHopper = ImmersiveBuilder.create(ImmersiveHandlers.hopperHandler)
            .setConfigChecker(() -> ActiveConfig.active().useHopperImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeHopper)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, 3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * -2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * -1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(0, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * 1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, -3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * 2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).build())
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_NO_DOWN)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();
    public static final BuiltImmersive<?,?> immersiveJukebox = ImmersiveBuilder.create(ImmersiveHandlers.jukeboxHandler)
            .setConfigChecker(() -> ActiveConfig.active().useJukeboxImmersion)
            .addHitbox(RelativeHitboxInfoBuilder.create(Vec3.ZERO, 0.125, 0.125, 0.625).build())
            .setPositioningMode(HitboxPositioningMode.TOP_LITERAL)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), 0, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .setVROnly(true)
            .build();

    public static final ImmersiveLever immersiveLever = new ImmersiveLever();

    public static final ImmersiveRepeater immersiveRepeater = new ImmersiveRepeater();
    public static final BuiltImmersive<ChestLikeData,?> immersiveShulker = ImmersiveBuilder.create(ImmersiveHandlers.shulkerBoxHandler, ChestLikeData.class)
            .setConfigChecker(() -> ActiveConfig.active().useShulkerImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeShulker)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(0));
            }, 0.14f).build(), 0.15)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(1));
            }, 0.14f).build(), 0.15)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(2));
            }, 0.14f).build(), 0.15)
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .setSlotActiveFunction((info, slot) -> ((ChestLikeData) info.getExtraData()).isOpen)
            .setOnRemove((info) -> ((ChestLikeData) info.getExtraData()).forceClose(info.getBlockPosition()))
            .setShouldRenderItemGuideFunction((info, slot) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return slot >= extra.currentRow * 9 && slot < (extra.currentRow + 1) * 9;
            })
            .build();

    public static final BuiltImmersive<?,?> immersiveSmithingTable = ImmersiveBuilder.create(ImmersiveHandlers.smithingTableHandler)
            .setConfigChecker(() -> ActiveConfig.active().useSmithingTableImmersion)
            .setRenderSize(ClientConstants.itemScaleSizeSmithingTable)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(-1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0, 0, 0.5), ClientConstants.itemScaleSizeSmithingTable / 1.025).holdsItems(true).triggerHitbox(true).itemSpins(true).itemRenderSizeMultiplier(1.5f).forceUpDownRenderDir(ForcedUpDownRenderDir.NULL).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slot, hand) -> {
                Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), slot, hand));
                return ClientConstants.defaultCooldownTicks;
            })
            .build();


    public static final BuiltImmersive<?,?> immersiveIronFurnacesFurnace = immersiveFurnace.getBuilderClone(ImmersiveHandlers.ironFurnacesFurnaceHandler)
            .setConfigChecker(() -> ActiveConfig.active().useIronFurnacesFurnaceImmersion)
            .build();

    public static final BuiltImmersive<?,?> immersiveTinkersConstructCraftingStation = immersiveCrafting.getBuilderClone(ImmersiveHandlers.tcCraftingStationHandler)
            .setConfigChecker(() -> ActiveConfig.active().useTinkersConstructCraftingStationImmersion)
            .modifyHitboxes(0, 8, (hitbox) -> hitbox.renderItem(false).build())
            .build();
}
