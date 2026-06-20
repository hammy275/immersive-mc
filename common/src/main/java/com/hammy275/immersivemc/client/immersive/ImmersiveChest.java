package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.client.immersive.info.render_state.ChestRenderState;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestStorage;
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
import java.util.Map;
import java.util.Objects;

import static com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage.CHEST_OPEN_THRESHOLD;

public class ImmersiveChest extends AbstractBlockBasedImmersive<ChestInfo, ChestRenderState, ChestStorage> {
    public static final double spacing = 3d/16d;

    @Override
    public void globalTick() {
        super.globalTick();
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
        if (!slotVisible(info.getDirectOpenness(), hitboxIndices.get(0))) return -1;
        ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), hitboxIndices, hand, false);
        return ImmersiveClientConstants.instance().defaultCooldown();
    }

    @Override
    public boolean shouldRender(ChestRenderState renderState) {
        return renderState.hasHitboxes();
    }

    @Override
    public void render(ChestRenderState renderState, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        for (int i = 0; i < 27; i++) {
            if (slotVisible(renderState.openness, i)) {
                int startTop = 9 * renderState.rowNum;
                int endTop = startTop + 9;
                boolean showCount = i >= startTop && i <= endTop;
                helpers.renderItemWithRenderState(renderState.items.get(i), stack, ClientConstants.itemScaleSizeChest,
                        showCount, renderState.light, renderState, true, i, null, renderState.forward, Direction.UP);
            }
        }

        if (renderState.hasOtherChest) {
            for (int i = 27; i < 27 * 2; i++) {
                if (slotVisible(renderState.openness, i)) {
                    int startTop = 9 * renderState.rowNum + 27;
                    int endTop = startTop + 9 + 27;
                    boolean showCount = i >= startTop && i <= endTop;
                    helpers.renderItemWithRenderState(renderState.items.get(i), stack, ClientConstants.itemScaleSizeChest,
                            showCount, renderState.light, renderState, true, i, null, renderState.forward, Direction.UP);
                }
            }
        }

        for (BoundingBox box : renderState.openCloseHitboxes) {
            helpers.renderHitbox(stack, box);
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
        Vec3 forward = Vec3.atLowerCornerOf(info.forward.getNormal());
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
        info.openCloseHitboxes.clear();
        info.openCloseHitboxes.add(OBBFactory.instance().create(aabbBase, xRot, Math.toRadians(info.forward.toYRot()), 0));
        if (openness == 1f) {
            double xSize, zSize;
            if (info.forward.getAxis() == Direction.Axis.X) {
                xSize = 1;
                zSize = info.otherChest != null ? 1.8 : 0.9;
            } else {
                xSize = info.otherChest != null ? 1.8 : 0.9;
                zSize = 1;
            }
            info.openCloseHitboxes.add(AABB.ofSize(info.openClosePosition.add(0, 0.425, 0).add(Vec3.atLowerCornerOf(info.forward.getNormal()).scale(0.65)),
                    xSize, 0.35, zSize));
        }
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
    public BlockBasedImmersiveHandler<ChestStorage> getHandler() {
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
    public void processStorageFromNetwork(ChestInfo info, ChestStorage storage) {
        for (Map.Entry<BlockPos, ListOfItemsStorage> entry : storage.items.entrySet()) {
            List<ItemStack> items = entry.getValue().getItems();
            if (entry.getKey().equals(info.otherPos)) {
                setItemsToHitboxes(info, 27, items);
            } else {
                setItemsToHitboxes(info, 0, items);
            }
        }
    }

    private void setItemsToHitboxes(ChestInfo info, int start, List<ItemStack> items) {
        for (int i = start; i < start + 27; i++) {
            info.hitboxes.get(i).item = items.get(i % 27);
        }
    }

    @Override
    public boolean isVROnly() {
        return false;
    }

    @Override
    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    @Override
    public void extractRenderState(ChestInfo info, ChestRenderState renderState, float partialTicks) {
        super.extractRenderState(info, renderState, partialTicks);
        renderState.hasOtherChest = info.otherChest != null;
        renderState.forward = info.forward;
        renderState.rowNum = info.getRowNum();
        if (info.openClosePosition != null) {
            renderState.openCloseHitboxes = List.copyOf(info.openCloseHitboxes);
        } else {
            renderState.openCloseHitboxes = List.of();
        }
        renderState.light = info.light;
        renderState.openness = info.getDirectOpenness();
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

    public static boolean slotVisible(float openness, int slot) {
        if (slot % 9 >= 6) { // Slot is in the front row
            return openness >= CHEST_OPEN_THRESHOLD;
        } else if (slot % 9 >= 3) { // Slot is in the middle row
            return openness >= 0.3f;
        } else { // Slot is in the back row
            return openness >= 0.5f;
        }
    }
}
