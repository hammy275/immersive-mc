package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.immersive.*;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.api_impl.ImmersiveMCClientRegistrationImpl;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.info.*;
import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.hammy275.immersivemc.client.ClientUtil.createConfigScreenInfo;

public class Immersives {

    public static final List<Immersive<? extends ImmersiveInfo, ? extends NetworkStorage>> IMMERSIVES =
            new ArrayList<>();
    public static final List<AbstractPlayerAttachmentImmersive<? extends AbstractPlayerAttachmentInfo, ? extends NetworkStorage>> IMMERSIVE_ATTACHMENTS =
            new ArrayList<>();

    public static final BuiltImmersive<?,?> immersiveAnvil = ImmersiveBuilder.create(ImmersiveHandlers.anvilHandler, AnvilData.class)
            .setRenderSize(ClientConstants.itemScaleSizeAnvil)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0, -1d/3d, 0), // When you place an anvil, the anvil's look direction is rotated 90 degrees.
                    ClientConstants.itemScaleSizeAnvil).rotateItem(ItemRotationType.CLOCKWISE).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0d, 0, 0),
                    ClientConstants.itemScaleSizeAnvil).rotateItem(ItemRotationType.CLOCKWISE).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> info.getItem(2).isEmpty() ? null : new Vec3(0, 1d/3d, 0),
                    ClientConstants.itemScaleSizeAnvil).rotateItem(ItemRotationType.CLOCKWISE).holdsItems(true).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0, 0, 0.5), 0)
                    .textSupplier((info) -> {
                        AnvilData data = (AnvilData) info.getExtraData();
                        if (data.anvilCost == 0) return null;
                        return List.of(new Pair<>(new TextComponent(I18n.get("immersivemc.immersive.anvil.levels_needed", data.anvilCost)), Vec3.ZERO));
                    })
                    .build())
            .setExtraStorageConsumer((storage, info) -> {
                AnvilStorage aStorage = (AnvilStorage) storage;
                AnvilData data = (AnvilData) info.getExtraData();
                data.anvilCost = aStorage.xpLevels;
            })
            .setPositioningMode(HitboxPositioningMode.TOP_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("anvil", () -> new ItemStack(Items.ANVIL),
                    config -> config.useAnvilImmersive,
                    (config, newVal) -> config.useAnvilImmersive = newVal))
            .build();
    public static final ImmersiveBackpack immersiveBackpack = new ImmersiveBackpack();
    public static final BuiltImmersive<ChestLikeData,?> immersiveBarrel = ImmersiveBuilder.create(ImmersiveHandlers.barrelHandler, ChestLikeData.class)
            .setRenderSize(ClientConstants.itemScaleSizeBarrel)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, 0.175).build(), ImmersiveChest.spacing)
            .addHitbox(RelativeHitboxInfoBuilder.create(new Vec3(0.25, 1d/16d, 0.15), 0.35, 0.35, 0.5)
                    .setVRMovementInfo(HitboxVRMovementInfoBuilder.create()
                            .axis(Direction.Axis.Z)
                            .threshold(0.05)
                            .controllerMode(HitboxVRMovementInfoBuilder.ControllerMode.EITHER)
                            .actionConsumer((info, hands) -> {
                                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                                extra.toggleOpen(info.getBlockPosition());
                            })
                            .build())
                    .build())
            .setPositioningMode(HitboxPositioningMode.BLOCK_FACING_NEG_X)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                slots = slots.stream().filter(slot -> slot < 27).toList();
                if (!slots.isEmpty()) {
                    ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                    return ImmersiveClientConstants.instance().defaultCooldown();
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
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("barrel", () -> new ItemStack(Items.BARREL),
                    config -> config.useBarrelImmersive,
                    (config, newVal) -> config.useBarrelImmersive = newVal))
            .build();
    public static final ImmersiveBeacon immersiveBeacon = new ImmersiveBeacon();
    public static final BuiltImmersive<?,?> immersiveBrewing = ImmersiveBuilder.create(ImmersiveHandlers.brewingStandHandler)
            .setRenderSize(ClientConstants.itemScaleSizeBrewing)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(-0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.active().autoCenterBrewingStandImmersive ? -1d/6d : -0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0.25, -1d/6d, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> new Vec3(0, ActiveConfig.active().autoCenterBrewingStandImmersive ? 0.1 : 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> ActiveConfig.active().autoCenterBrewingStandImmersive ? new Vec3(0, 0.35, 0) : new Vec3(-0.25, 0.25, 0),
                    ClientConstants.itemScaleSizeBrewing / 1.5).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("brewing", () -> new ItemStack(Items.BREWING_STAND),
                    config -> config.useBrewingStandImmersive,
                    (config, newVal) -> config.useBrewingStandImmersive = newVal))
            .build();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final BuiltImmersive<?,?> immersiveCrafting = ImmersiveBuilder.create(ImmersiveHandlers.craftingHandler)
            .setRenderSize(ClientConstants.itemScaleSizeCrafting)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO,
                            ClientConstants.itemScaleSizeCrafting / 1.5f).needs3DResourcePackCompat(true).build(),
                    3d / 16d)
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> info.getItem(9).isEmpty() ? null : new Vec3(0, 0, 0.5),
                    ClientConstants.itemScaleSizeCrafting * 1.5d).holdsItems(true)
                    .itemSpins(true).itemRenderSizeMultiplier(3f).triggerHitbox(true)
                    .forceUpDownRenderDir(ForcedUpDownRenderDir.NULL).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setConfigScreenInfo(createConfigScreenInfo("crafting", () -> new ItemStack(Items.CRAFTING_TABLE),
                    config -> config.useCraftingTableImmersive,
                    (config, newVal) -> config.useCraftingTableImmersive = newVal))
            .build();
    public static final ImmersiveDoor immersiveDoor = new ImmersiveDoor();
    public static final BuiltImmersive<EnchantingData, ETableStorage> immersiveETable = ImmersiveBuilder.create(ImmersiveHandlers.enchantingTableHandler, EnchantingData.class)
            .setRenderSize(ClientConstants.itemScaleSizeETable)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(0, 0.9, -0.5), ClientConstants.itemScaleSizeETable).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                if (info.getItem(0).isEmpty()) return null;
                double rad = Math.PI * 2 * (info.ticksExisted() % (20d * 15d) / (20d * 15d));
                double x = Math.sin(rad);
                double z = Math.cos(rad) - 1;
                double yProgress = (info.ticksExisted() % 40d) / 40d;
                return new Vec3(x, 1.25, z).add(0, Math.sin(yProgress * Math.PI * 2) * 0.05, 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).weakData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(1) && (!VRVerify.clientInVR() || (
                        Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(-1), info.getAllHitboxes().get(1).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(0), info.getAllHitboxes().get(1).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(1), info.getAllHitboxes().get(1).getHitbox()).isEmpty()))) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(new TextComponent(data.levelsNeeded + " (1)"), new Vec3(0, 0.33, 0)));
                    for (int i = 0; i < data.textPreviews.size(); i++) {
                        texts.add(new Pair<>(data.textPreviews.get(i), new Vec3(0, -0.16 - 0.16 * (i + 1), 0)));
                    }
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(new TranslatableComponent("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                if (info.getItem(0).isEmpty()) return null;
                double rad = Math.PI * 2 * ((info.ticksExisted() + (20d * 5d)) % (20d * 15d) / (20d * 15d));
                double x = Math.sin(rad);
                double z = Math.cos(rad) - 1;
                double yProgress = (info.ticksExisted() % 40d) / 40d;
                return new Vec3(x, 1.25, z).add(0, Math.sin(yProgress * Math.PI * 2) * 0.05, 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).midData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(2) && (!VRVerify.clientInVR() || (
                        Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(-1), info.getAllHitboxes().get(2).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(0), info.getAllHitboxes().get(2).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(1), info.getAllHitboxes().get(2).getHitbox()).isEmpty()))) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(new TextComponent(data.levelsNeeded + " (2)"), new Vec3(0, 0.33, 0)));
                    for (int i = 0; i < data.textPreviews.size(); i++) {
                        texts.add(new Pair<>(data.textPreviews.get(i), new Vec3(0, -0.16 - 0.16 * (i + 1), 0)));
                    }
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(new TranslatableComponent("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                if (info.getItem(0).isEmpty()) return null;
                double rad = Math.PI * 2 * ((info.ticksExisted() + (20d * 10d)) % (20d * 15d) / (20d * 15d));
                double x = Math.sin(rad);
                double z = Math.cos(rad) - 1;
                double yProgress = (info.ticksExisted() % 40d) / 40d;
                return new Vec3(x, 1.25, z).add(0, Math.sin(yProgress * Math.PI * 2) * 0.05, 0);
            }, ClientConstants.itemScaleSizeETable).holdsItems(true).textSupplier((info) -> {
                EnchantingData.ETableData data = ((EnchantingData) info.getExtraData()).strongData;
                List<Pair<Component, Vec3>> texts = new ArrayList<>();
                if (!info.isSlotHovered(3) && (!VRVerify.clientInVR() || (
                        Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(-1), info.getAllHitboxes().get(3).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(0), info.getAllHitboxes().get(3).getHitbox()).isEmpty() &&
                                Util.rayTraceClosest(ClientUtil.getVRStartAndEnd(1), info.getAllHitboxes().get(3).getHitbox()).isEmpty()))) {
                    return null;
                }
                if (data.isPresent()) {
                    texts.add(new Pair<>(new TextComponent(data.levelsNeeded + " (3)"), new Vec3(0, 0.33, 0)));
                    for (int i = 0; i < data.textPreviews.size(); i++) {
                        texts.add(new Pair<>(data.textPreviews.get(i), new Vec3(0, -0.16 - 0.16 * (i + 1), 0)));
                    }
                } else if (info.getItem(0) != null && !info.getItem(0).isEmpty()) {
                    texts.add(new Pair<>(new TranslatableComponent("immersivemc.immersive.etable.no_ench"), new Vec3(0, -0.33, 0)));
                }
                return texts;
            }).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setExtraStorageConsumer((storage, info) -> {
                EnchantingData extraData = info.getExtraData();
                extraData.weakData.set(storage.slots[0]);
                extraData.midData.set(storage.slots[1]);
                extraData.strongData.set(storage.slots[2]);
                extraData.apothStats = storage.apothStats;
                for (int i = 1; i <= 3; i++) {
                    EnchantingData.ETableData data = i == 1 ? extraData.weakData : i == 2 ? extraData.midData : extraData.strongData;
                    ItemStack item = info.getItem(0);
                    if (item != null && !item.isEmpty()) {
                        item = item.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : item.copy();
                        if (data.isPresent()) {
                            EnchantmentHelper.setEnchantments(ClientConstants.fakeEnch, item);
                        }
                    }
                    if (info.getExtraData().hasAnyEnchantments()) {
                        info.setFakeItem(i, item);
                    } else {
                        info.setFakeItem(i, ItemStack.EMPTY);
                    }
                }
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("enchanting_table", () -> new ItemStack(Items.ENCHANTING_TABLE),
                    config -> config.useEnchantingTableImmersive,
                    (config, newVal) -> config.useEnchantingTableImmersive = newVal))
            .setExtraRenderer((info, stack, helpers, partialTick, light) -> {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition());
                if (Apoth.apothImpl.enchantModuleEnabled() && blockEntity instanceof EnchantmentTableBlockEntity table && table.open == 1f) {
                    Optional<BuiltImmersiveInfo<EnchantingData>> infoOpt = Immersives.immersiveETable.getTrackedObjects().stream()
                            .filter(i -> i.getBlockPosition().equals(table.getBlockPos()))
                            .findFirst();
                    if (infoOpt.isPresent()) {
                        BlockPos pos = table.getBlockPos();
                        Player player = Minecraft.getInstance().level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
                        if (player != null) {
                            ClientBookData bookData = info.getExtraData().getBookData(info);
                            if (bookData != null) {
                                float rot = table.rot + (float) Math.PI / 2f;
                                PosRot posRot = new PosRot(Vec3.atBottomCenterOf(pos).add(0, 0.9, 0),
                                        Util.getLookAngle((float) -Math.PI / 8f, -rot),
                                        (float) Math.PI / 8f, rot, 0);
                                bookData.render(stack, light, posRot);
                            }
                        }
                    }
                }
            })
            .build();
    public static final BuiltImmersive<?,?> immersiveFurnace = ImmersiveBuilder.create(ImmersiveHandlers.furnaceHandler)
            .setRenderSize(ClientConstants.itemScaleSizeFurnace)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                if (ActiveConfig.active().autoCenterFurnaceImmersive) {
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
                if (ActiveConfig.active().autoCenterFurnaceImmersive) {
                    return new Vec3(0, -0.25, 0);
                } else {
                    return new Vec3(-0.25, -0.25, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).needs3DResourcePackCompat(true).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> {
                if (ActiveConfig.active().autoCenterFurnaceImmersive) {
                    if (info.getItem(2) == null || info.getItem(2).isEmpty()) {
                        return null;
                    } else if (info.getItem(0) == null || info.getItem(0).isEmpty()) {
                        return new Vec3(0, 0.25, 0);
                    } else {
                        return new Vec3(0.25, 0.25, 0);
                    }
                } else {
                    return info.getItem(2).isEmpty() ? null : new Vec3(0.25, 0, 0);
                }},
                    ClientConstants.itemScaleSizeFurnace / 1.5d).holdsItems(true).needs3DResourcePackCompat(true).build())
            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                if (ActiveConfig.active().autoCenterFurnaceImmersive) {
                    int slot = slots.get(0);
                    if (info.getAllHitboxes().get(0).getHitbox() == null && slot == 2) {
                        ItemStack handItem = player.getItemInHand(hand);
                        if (!handItem.isEmpty() &&
                                (!Util.stacksEqualBesidesCount(handItem, info.getItem(2)) || handItem.getCount() == handItem.getMaxStackSize())) {
                            // If we don't have an input slot, set to the input slot instead of output if:
                            // Our hand is NOT empty (we have something to put in) AND
                            // We're holding a different item than what's in the output OR what we have in our hand can't be added to
                            slots = List.of(0);
                        }
                    }
                }
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("furnace", () -> new ItemStack(Items.FURNACE),
                    config -> config.useFurnaceImmersive,
                    (config, newVal) -> config.useFurnaceImmersive = newVal))
            .build();
    public static final BuiltImmersive<GrindstoneData,?> immersiveGrindstone = ImmersiveBuilder.create(ImmersiveHandlers.grindstoneHandler, GrindstoneData.class)
            .setRenderSize(ClientConstants.itemScaleSizeGrindstone)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(info -> grindGrindstone() ? null : new Vec3(0, 0.5, -0.65), ClientConstants.itemScaleSizeGrindstone).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(info -> grindGrindstone() ? null : new Vec3(0, 0.5, -0.35), ClientConstants.itemScaleSizeGrindstone).build())
            .addHitbox(RelativeHitboxInfoBuilder.create(info -> grindGrindstone() || info.getItem(2).isEmpty() ? null : new Vec3(0, 0.125, -0.125),
                            ClientConstants.itemScaleSizeGrindstone * 1.5d)
                    .holdsItems(true).itemRenderSizeMultiplier(2f)
                    .forceUpDownRenderDir(info -> Minecraft.getInstance().level.getBlockState(info.getBlockPosition()).getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.WALL
                    ? ForcedUpDownRenderDir.DOWN : ForcedUpDownRenderDir.NULL)
                    .build())
            .addHitbox(RelativeHitboxInfoBuilder.create(info -> !grindGrindstone() ? null : new Vec3(0, 0.125, -0.5), 0.875)
                    .setVRMovementInfo(HitboxVRMovementInfoBuilder.create()
                            .axis(null)
                            .controllerMode(HitboxVRMovementInfoBuilder.ControllerMode.EITHER)
                            .threshold(0.03)
                            .actionConsumer((info, hands) -> {
                                GrindstoneData data = (GrindstoneData) info.getExtraData();
                                for (InteractionHand hand : hands) {
                                    boolean didTick = data.grindTick(hand);
                                    if (data.didGrind(hand)) {
                                        ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), List.of(3), hand, false);
                                        data.resetGrind(hand);
                                    } else if (didTick) {
                                        int numParticles = ThreadLocalRandom.current().nextInt(1, 5);
                                        Vec3 pos = VR.ClientAPI.getPreTickWorldPose().getHand(hand).getPos();
                                        for (int i = 0; i < numParticles; i++) {
                                            Minecraft.getInstance().level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                                    pos.x, pos.y, pos.z,
                                                    ThreadLocalRandom.current().nextDouble() - 0.5,
                                                    0.2,
                                                    ThreadLocalRandom.current().nextDouble() - 0.5);
                                        }
                                    }
                                }
                            })
                            .build())
                    .build())

            .setPositioningMode(HitboxPositioningMode.HORIZONTAL_BLOCK_FACING_ATTACHED_FLOOR_CEILING_REVERSED)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                slots = slots.stream().filter(s -> s != 3).toList();
                if (!slots.isEmpty()) {
                    ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                    return ImmersiveClientConstants.instance().defaultCooldown();
                }
                return -1;
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("grindstone", () -> new ItemStack(Items.GRINDSTONE),
                    config -> config.useGrindstoneImmersive,
                    (config, newVal) -> config.useGrindstoneImmersive = newVal))
            .build();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final BuiltImmersive<?,?> immersiveHopper = ImmersiveBuilder.create(ImmersiveHandlers.hopperHandler)
            .setRenderSize(ClientConstants.itemScaleSizeHopper)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, 3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * -2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).lerps(false).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * -1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).lerps(false).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(0, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).lerps(false).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                return new Vec3(ClientConstants.itemScaleSizeHopper * 1.1d, forward == Direction.UP ? 0 : 0.375, 0);
            }, ClientConstants.itemScaleSizeHopper).lerps(false).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                Direction forward = Util.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
                if (forward == Direction.UP) {
                    return new Vec3(0, -3d/16d, 0);
                } else {
                    return new Vec3(ClientConstants.itemScaleSizeHopper * 2.2d, 0.375, 0);
                }
            }, ClientConstants.itemScaleSizeHopper).lerps(false).build())
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_NO_DOWN)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setConfigScreenInfo(createConfigScreenInfo("hopper", () -> new ItemStack(Items.HOPPER),
                    config -> config.useHopperImmersive,
                    (config, newVal) -> config.useHopperImmersive = newVal))
            .build();
    public static final BuiltImmersive<?,?> immersiveJukebox = ImmersiveBuilder.create(ImmersiveHandlers.jukeboxHandler)
            .addHitbox(RelativeHitboxInfoBuilder.create(Vec3.ZERO, 0.125, 0.125, 0.625).build())
            .setPositioningMode(HitboxPositioningMode.TOP_LITERAL)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), List.of(0), hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setNoDragHitbox()
            .setVROnly(true)
            .setConfigScreenInfo(createConfigScreenInfo("jukebox", () -> new ItemStack(Items.JUKEBOX),
                    config -> config.useJukeboxImmersive,
                    (config, newVal) -> config.useJukeboxImmersive = newVal))
            .build();

    public static final ImmersiveLectern immersiveLectern = new ImmersiveLectern();
    public static final ImmersiveLever immersiveLever = new ImmersiveLever();

    public static final ImmersiveRepeater immersiveRepeater = new ImmersiveRepeater();
    public static final BuiltImmersive<ChestLikeData,?> immersiveShulker = ImmersiveBuilder.create(ImmersiveHandlers.shulkerBoxHandler, ChestLikeData.class)
            .setRenderSize(ClientConstants.itemScaleSizeShulker)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(0));
            }, 0.14f).lerps(false).build(), 0.15)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(1));
            }, 0.14f).lerps(false).build(), 0.15)
            .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput((info) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return new Vec3(0, 0.25, -1d/3d * extra.offsetIn(2));
            }, 0.14f).lerps(false).build(), 0.15)
            .setPositioningMode(HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setSlotActiveFunction((info, slot) -> ((ChestLikeData) info.getExtraData()).isOpen)
            .setOnRemove((info) -> ((ChestLikeData) info.getExtraData()).forceClose(info.getBlockPosition()))
            .setShouldRenderItemGuideFunction((info, slot) -> {
                ChestLikeData extra = (ChestLikeData) info.getExtraData();
                return slot >= extra.currentRow * 9 && slot < (extra.currentRow + 1) * 9;
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("shulker", () -> new ItemStack(Items.SHULKER_BOX),
                    config -> config.useShulkerImmersive,
                    (config, newVal) -> config.useShulkerImmersive = newVal))
            .build();

    public static final BuiltImmersive<?,?> immersiveSmithingTable = ImmersiveBuilder.create(ImmersiveHandlers.smithingTableHandler)
            .setRenderSize(ClientConstants.itemScaleSizeSmithingTable)
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(new Vec3(-1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, ClientConstants.itemScaleSizeSmithingTable / 1.025).build())
            .addHitbox(RelativeHitboxInfoBuilder.create((info) -> info.getItem(2).isEmpty() ? null : new Vec3(1d/3d, 0, 0), ClientConstants.itemScaleSizeSmithingTable / 1.025).holdsItems(true).build())
            .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
            .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                return ImmersiveClientConstants.instance().defaultCooldown();
            })
            .setNoDragHitbox()
            .setConfigScreenInfo(createConfigScreenInfo("smithing_table", () -> new ItemStack(Items.SMITHING_TABLE),
                    config -> config.useSmithingTableImmersive,
                    (config, newVal) -> config.useSmithingTableImmersive = newVal))
            .build();

    public static final Immersive<?, ?> immersiveTrapdoor = new ImmersiveTrapdoor();

    public static final BuiltImmersive<?,?> immersiveIronFurnacesFurnace = CompatModule.create(
            immersiveFurnace.getBuilderClone(ImmersiveHandlers.ironFurnacesFurnaceHandler).build(),
            BuiltImmersive.class,
            IronFurnaces.compatData);

    public static final BuiltImmersive<?,?> immersiveTinkersConstructCraftingStation = CompatModule.create(
            immersiveCrafting.getBuilderClone(ImmersiveHandlers.tcCraftingStationHandler)
            .modifyHitboxes(0, 8, (hitbox) -> hitbox.renderItem(false).build())
            .build(),
            BuiltImmersive.class,
            TinkersConstruct.compatData
            );

    public static final BuiltImmersive<?,?> immersiveApothSalvagingTable = CompatModule.create(
            ImmersiveBuilder.create(ImmersiveHandlers.apothSalvagingTableHandler)
                    .setRenderSize(ClientConstants.itemScaleSizeApothSalvagingTable)
                    .add3x3Grid(RelativeHitboxInfoBuilder.createItemInput(Vec3.ZERO, ClientConstants.itemScaleSizeApothSalvagingTable).build(),
                            ClientConstants.itemScaleSizeApothSalvagingTable)
                    .setPositioningMode(HitboxPositioningMode.TOP_PLAYER_FACING)
                    .setHitboxInteractHandler((info, player, slots, hand, modifierPressed) -> {
                        ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), slots, hand, modifierPressed);
                        return ImmersiveClientConstants.instance().defaultCooldown();
                    })
                    .build(),
            BuiltImmersive.class,
            Apoth.compatData
    );

    public static final BuiltImmersive<?,?> immersiveVisualWorkbench =
            immersiveTinkersConstructCraftingStation.getBuilderClone(ImmersiveHandlers.visualWorkbenchHandler)
                    .modifyHitbox(9, hitbox -> hitbox.renderItem(false).build())
                    .build();

    static {
        ImmersiveMCClientRegistrationImpl.doImmersiveRegistration((immersive) -> {
            if (!IMMERSIVES.contains(immersive)) {
                IMMERSIVES.add(immersive);
            }
        });
    }

    private static boolean grindGrindstone() {
        return VRVerify.clientInVR() && ActiveConfig.active().useGrindMotionGrindstoneInVR;
    }
}
