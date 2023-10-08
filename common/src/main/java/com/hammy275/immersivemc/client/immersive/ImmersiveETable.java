package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.EnchantingInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.GetEnchantmentsPacket;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImmersiveETable extends AbstractWorldStorageImmersive<EnchantingInfo> {

    private static final Map<Enchantment, Integer> fakeEnch = new HashMap<>();
    static {
        fakeEnch.put(Enchantments.MENDING, 32767); // Just for the glimmer effect.
    }

    protected final float[] yOffsets;

    protected int noInfosCooldown = 0;

    public ImmersiveETable() {
        super(1); // Enchanting tables are special, let's only have one active
        List<Float> yOffsets = new ArrayList<>();
        float max = 0.25f;
        for (float i = 0; i <= max; i += max / 20f) {
            yOffsets.add(i - (max / 2f));
        }
        for (float i = 0.25f; i >= 0f; i -= max / 20f) {
            yOffsets.add(i - (max / 2f));
        }
        this.yOffsets = new float[yOffsets.size()];
        for (int i = 0; i < yOffsets.size(); i++) {
            this.yOffsets[i] = yOffsets.get(i);
        }
    }

    @Override
    protected void doTick(EnchantingInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;

        for (int x = -1; x <= 1; x++) { // 3x3 area one block and two blocks above must all be air to look nice
            for (int y = 1; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!Minecraft.getInstance().level.getBlockState(info.getBlockPosition().offset(x, y, z)).canBeReplaced()) {
                        info.areaAboveIsAir = false;
                        return;
                    }
                }
            }
        }
        info.areaAboveIsAir = true;

        for (int i = 0; i < info.yOffsetPositions.length; i++) {
            if (++info.yOffsetPositions[i] >= this.yOffsets.length) {
                info.yOffsetPositions[i] = 0;
            }
        }

        float hitboxSize = ClientConstants.itemScaleSizeETable / 2f;

        Vec3 inp = Vec3.upFromBottomCenterOf(info.getBlockPosition(), 1.25);
        info.setPosition(0, inp.add(this.getYDiffFromOffset(info, 0)));
        info.setHitbox(0, createHitbox(inp, hitboxSize));

        Direction facing = getForwardFromPlayer(Minecraft.getInstance().player);
        Direction rightFromMid = facing.getClockWise(); // From the perspective of the ETable facing the player
        Direction leftFromMid = facing.getCounterClockWise();

        Vec3 midItem = inp.add(0, 0.5, 0);
        Vec3 unit = new Vec3(rightFromMid.getNormal().getX(), rightFromMid.getNormal().getY(),
                rightFromMid.getNormal().getZ());
        Vec3 weakItem = midItem.add(unit.multiply(0.5, 0.5, 0.5));

        unit = new Vec3(leftFromMid.getNormal().getX(), leftFromMid.getNormal().getY(),
                leftFromMid.getNormal().getZ());
        Vec3 strongItem = midItem.add(unit.multiply(0.5, 0.5, 0.5));

        info.setPosition(1, weakItem.add(this.getYDiffFromOffset(info, 1)));
        info.setPosition(2, midItem.add(this.getYDiffFromOffset(info, 2)));
        info.setPosition(3, strongItem.add(this.getYDiffFromOffset(info, 3)));
        info.setHitbox(1, createHitbox(weakItem, hitboxSize));
        info.setHitbox(2, createHitbox(midItem, hitboxSize));
        info.setHitbox(3, createHitbox(strongItem, hitboxSize));

        // Determine which floating item is being looked at
        if (Minecraft.getInstance().gameMode == null) return;
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        info.lookingAtIndex = -1;

        Player player = Minecraft.getInstance().player;
        Vec3 start = player.getEyePosition(1);
        Vec3 viewVec = player.getViewVector(1);
        Vec3 end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);
        Optional<Integer> closest = Util.rayTraceClosest(start, end,
                info.getHitbox(1), info.getHitbox(2), info.getHitbox(3));
        closest.ifPresent(targetSlot -> info.lookingAtIndex = targetSlot);
        if (info.ticksActive % (ClientConstants.inventorySyncTime * 4) == 0) {
            Network.INSTANCE.sendToServer(new GetEnchantmentsPacket(info.getBlockPosition()));
        }
    }

    @Override
    public BlockPos getLightPos(EnchantingInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public boolean shouldRender(EnchantingInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                Minecraft.getInstance().level != null &&
                info.readyToRender();
    }

    @Override
    protected void render(EnchantingInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeETable / info.getItemTransitionCountdown();

        if (!info.itemEnchantedCopy.isEmpty()) {
            for (int i = 0; i <= 2; i++) {
                EnchantingInfo.ETableInfo enchInfo =
                        i == 0 ? info.weakInfo : i == 1 ? info.midInfo : info.strongInfo;
                float renderSize = info.slotHovered == i + 1 ? itemSize * 1.25f : itemSize;
                renderItem(info.itemEnchantedCopy, stack, info.getPosition(i + 1), renderSize,
                        getForwardFromPlayer(Minecraft.getInstance().player), info.getHitbox(i + 1), false, info.light);
                if (info.lookingAtIndex == i) {
                    if (enchInfo.isPresent()) {
                        renderText(Component.literal(enchInfo.levelsNeeded + " (" + (i + 1) + ")"),
                                stack,
                                info.getPosition(i + 1).add(0, 0.33, 0), info.light);
                        renderText(enchInfo.textPreview,
                                stack,
                                info.getPosition(i + 1).add(0, -0.33, 0), info.light);
                    } else {
                        renderText(Component.translatable("immersivemc.immersive.etable.no_ench"),
                                stack, info.getPosition(i + 1).add(0, -0.2, 0), info.light);
                    }
                }

            }
        }
        float renderSize = info.slotHovered == 0 ? itemSize * 1.25f : itemSize;
        if (info.items[0] != null && !info.items[0].isEmpty()) {
            renderItem(info.items[0], stack, info.getPosition(0), renderSize,
                    getForwardFromPlayer(Minecraft.getInstance().player), info.getHitbox(0), false, info.light);
        } else {
            renderHitbox(stack, info.getHitbox(0), info.getPosition(0));
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useETableImmersion;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo infoRaw, ImmersiveStorage storage) {
        EnchantingInfo info = (EnchantingInfo) infoRaw;
        info.items[0] = storage.getItem(0);
        if (info.items[0] != null && !info.items[0].isEmpty()) {
            if (info.items[0].getItem() == Items.BOOK) {
                info.itemEnchantedCopy = new ItemStack(Items.ENCHANTED_BOOK);
            } else {
                info.itemEnchantedCopy = info.items[0].copy();
            }
            EnchantmentHelper.setEnchantments(fakeEnch, info.itemEnchantedCopy);
        } else {
            info.itemEnchantedCopy = ItemStack.EMPTY;
        }

    }

    @Override
    public EnchantingInfo getNewInfo(BlockPos pos) {
        return new EnchantingInfo(pos, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderETable;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(EnchantingInfo info, int slotNum) {
        return info.items[0] == null || info.items[0].isEmpty();
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isEnchantingTable(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    protected Vec3 getYDiffFromOffset(EnchantingInfo info, int slot) {
        return new Vec3(0, this.yOffsets[info.yOffsetPositions[slot]], 0);
    }

    @Override
    protected void initInfo(EnchantingInfo info) {
        // NOOP: Changes based on the items moving up and down to look cool
    }
}
