package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class ImmersiveChest extends AbstractImmersive<ChestInfo, ListOfItemsStorage> {
    public static final double spacing = 3d/16d;
    private final double threshold = 0.03;
    // Intentionally stored outside infos, so a chest close (which removes the info) will still have a cooldown
    // before you can open a chest again.
    public int openCloseCooldown = 0;

    @Override
    public void globalTick() {
        super.globalTick();
        if (openCloseCooldown > 0) {
            openCloseCooldown--;
        }
        this.infos.removeIf((info) -> !chestsValid(info));
    }

    @Override
    public ChestInfo buildInfo(BlockPos pos, Level level) {
        BlockEntity blockEnt = level.getBlockEntity(pos);
        if (blockEnt instanceof ChestBlockEntity) {
            return new ChestInfo(blockEnt, Util.getOtherChest((ChestBlockEntity) blockEnt));
        } else if (blockEnt instanceof EnderChestBlockEntity) {
            return new ChestInfo(blockEnt, null);
        }
        throw new IllegalArgumentException("ImmersiveChest can only track chests and ender chests!");
    }

    @Override
    public int handleHitboxInteract(ChestInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        if (!info.slotVisible(hitboxIndices.get(0))) return -1;
        ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), hitboxIndices, hand, false);
        return ImmersiveClientConstants.instance().defaultCooldown();
    }

    @Override
    public boolean shouldRender(ChestInfo info) {
        return info.hasHitboxes();
    }

    @Override
    public void render(ChestInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        for (int i = 0; i < 27; i++) {
            if (info.slotVisible(i)) {
                int startTop = 9 * info.getRowNum();
                int endTop = startTop + 9;
                boolean showCount = i >= startTop && i <= endTop;
                helpers.renderItemWithInfo(info.hitboxes.get(i).item, stack, ClientConstants.itemScaleSizeChest,
                        showCount, info.light, info, true, i, null, info.forward, Direction.UP);
            }
        }

        if (info.otherChest != null) {
            for (int i = 27; i < 27 * 2; i++) {
                if (info.slotVisible(i)) {
                    int startTop = 9 * info.getRowNum() + 27;
                    int endTop = startTop + 9 + 27;
                    boolean showCount = i >= startTop && i <= endTop;
                    helpers.renderItemWithInfo(info.hitboxes.get(i).item, stack, ClientConstants.itemScaleSizeChest,
                            showCount, info.light, info, true, i, null, info.forward, Direction.UP);
                }
            }
        }

        if (info.openCloseHitbox != null && info.openClosePosition != null) {
            helpers.renderHitbox(stack, info.openCloseHitbox);
        }
    }

    @Override
    public void tick(ChestInfo info) {
        super.tick(info);
        info.light = ImmersiveClientLogicHelpers.instance().getLight(info.getBlockPosition().above());
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof ChestBlockEntity cbe) {
            info.otherChest = Util.getOtherChest(cbe);
            if (info.otherChest != null) {
                info.otherPos = info.otherChest.getBlockPos();
            } else {
                info.otherPos = null;
            }
        }

        BlockEntity[] chests = new BlockEntity[]{info.chest, info.otherChest};
        for (int i = 0; i <= 1; i++) {
            BlockEntity chest = chests[i];
            if (chest == null) continue;
            info.forward = chest.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

            Vec3[] positions = Util.get3x3HorizontalGrid(chest.getBlockPos(), spacing, info.forward,
                    false);
            float hitboxSize = ClientConstants.itemScaleSizeChest / 3f * 2.2f;
            int startTop = 9 * info.getRowNum() + 27 * i;
            int endTop = startTop + 9;
            for (int z = startTop; z < endTop; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startTop) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.2 + offset, offset),
                        hitboxSize, hitboxSize, hitboxSize);
            }

            int startMid = 9 * info.getNextRow(info.getRowNum()) + 27 * i;
            int endMid = startMid + 9;
            for (int z = startMid; z < endMid; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startMid) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.325 + offset, offset),
                        0, 0, 0);
            }

            int startBot = 9 * info.getNextRow(info.getNextRow(info.getRowNum())) + 27 * i;
            int endBot = startBot + 9;
            for (int z = startBot; z < endBot; z++) {
                Vec3 pos = positions[z % 9];
                double offset = (z - startBot) * 0.0002; //Minor offset for each hitbox to prevent Z-fighting
                info.getAllHitboxes().get(z).box = AABB.ofSize(pos.add(offset, -0.45 + offset, offset),
                        0, 0, 0);
            }
        }

        float openness = Util.getChestLidController(info.chest).immersiveMC$getOpenness();
        openness = 1f - openness;
        openness = 1f - openness * openness * openness;
        Vec3 forward = info.forward.getUnitVec3();
        Vec3 chestBackTopPos = Vec3.atBottomCenterOf(info.chest.getBlockPos()).add(forward.scale(-0.5)).add(0, 10d/16d, 0);


        Vector3f lidVecF = new Vector3f(0, 0, 1);
        float xRot = openness * (float) Math.PI / 2f;
        lidVecF.rotate(Axis.XN.rotation(xRot));
        lidVecF.rotate(Axis.YN.rotationDegrees(info.forward.toYRot()));
        Vec3 lidVec = new Vec3(lidVecF.x, lidVecF.y, lidVecF.z);
        info.openClosePosition = chestBackTopPos.add(lidVec.scale(0.5));
        if (info.otherChest != null) {
            info.openClosePosition = info.openClosePosition.add(Vec3.atLowerCornerOf(info.otherPos.subtract(info.getBlockPosition())).scale(0.5));
        }
        AABB aabbBase = AABB.ofSize(info.openClosePosition, info.otherChest != null ? 1.8 : 0.9, 0.3, 1.2);
        info.openCloseHitbox = OBBFactory.instance().create(aabbBase, xRot, Math.toRadians(info.forward.toYRot()), 0);

    }

    @Override
    @Nullable
    public AABB getDragHitbox(ChestInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(ChestInfo info, int hitboxIndex) {
        return true;
    }

    @Override
    public ImmersiveHandler<ListOfItemsStorage> getHandler() {
        return ImmersiveHandlers.chestHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("chest", () -> new ItemStack(Items.CHEST),
                config -> config.useChestImmersive,
                (config, newVal) -> config.useChestImmersive = newVal);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(ChestInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(ChestInfo info, ListOfItemsStorage storage) {
        for (int i = 0; i < storage.getItems().size(); i++) {
            info.hitboxes.get(i).item = storage.getItems().get(i);
        }
    }

    @Override
    public boolean isVROnly() {
        return false;
    }

    public boolean chestsValid(ChestInfo info) {
        boolean mainChestExists = getHandler().isValidBlock(info.getBlockPosition(), info.chest.getLevel());
        boolean otherChestExists = info.otherChest == null ||
                (info.chest.getLevel() != null && info.chest.getLevel().getBlockEntity(info.otherPos) instanceof ChestBlockEntity);
        return mainChestExists && otherChestExists;
    }

    public static ChestInfo findImmersive(BlockEntity chest) {
        Objects.requireNonNull(chest);
        for (ChestInfo info : Immersives.immersiveChest.getTrackedObjects()) {
            if (info.chest == chest || info.otherChest == chest) {
                return info;
            }
        }
        return null;
    }

    public static void openChest(ChestInfo info) {
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(info.getBlockPosition(), !info.isOpen()));
        if (info.isOpen()) {
            Lootr.lootrImpl.markOpener(Minecraft.getInstance().player, info.getBlockPosition());
        }
    }
}
