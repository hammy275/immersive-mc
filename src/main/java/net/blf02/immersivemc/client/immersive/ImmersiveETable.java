package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.blf02.immersivemc.client.immersive.info.EnchantingInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.GetEnchantmentsPacket;
import net.blf02.immersivemc.common.network.packet.InteractPacket;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.common.vr.util.Util;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.*;

public class ImmersiveETable extends AbstractWorldStorageImmersive<EnchantingInfo> {

    private static final Map<Enchantment, Integer> fakeEnch = new HashMap<>();
    static {
        fakeEnch.put(Enchantments.MENDING, 32767); // Just for the glimmer effect.
    }

    protected final float[] yOffsets;

    public static final ImmersiveETable singleton = new ImmersiveETable();

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
                    if (!Minecraft.getInstance().level.getBlockState(info.getBlockPosition().offset(x, y, z)).isAir()) {
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

        Vector3d inp = Vector3d.upFromBottomCenterOf(info.getBlockPosition(), 1.25);
        info.setPosition(0, inp.add(this.getYDiffFromOffset(info, 0)));
        info.setHitbox(0, createHitbox(inp, hitboxSize));

        Direction facing = getForwardFromPlayer(Minecraft.getInstance().player);
        Direction rightFromMid = facing.getClockWise(); // From the perspective of the ETable facing the player
        Direction leftFromMid = facing.getCounterClockWise();

        Vector3d midItem = inp.add(0, 0.5, 0);
        Vector3d unit = new Vector3d(rightFromMid.getNormal().getX(), rightFromMid.getNormal().getY(),
                rightFromMid.getNormal().getZ());
        Vector3d weakItem = midItem.add(unit.multiply(0.5, 0.5, 0.5));

        unit = new Vector3d(leftFromMid.getNormal().getX(), leftFromMid.getNormal().getY(),
                leftFromMid.getNormal().getZ());
        Vector3d strongItem = midItem.add(unit.multiply(0.5, 0.5, 0.5));

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

        PlayerEntity player = Minecraft.getInstance().player;
        Vector3d start = player.getEyePosition(1);
        Vector3d viewVec = player.getViewVector(1);
        Vector3d end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);
        Optional<Integer> closest = Util.rayTraceClosest(start, end,
                info.getHitbox(1), info.getHitbox(2), info.getHitbox(3));
        closest.ifPresent(targetSlot -> info.lookingAtIndex = targetSlot);
        if (info.ticksActive % (ClientConstants.inventorySyncTime * 4) == 0) {
            Network.INSTANCE.sendToServer(new GetEnchantmentsPacket(info.getBlockPosition()));
        }
    }

    @Override
    public boolean hasValidBlock(EnchantingInfo info, World level) {
        return level.getBlockState(info.getBlockPosition()).getBlock() instanceof EnchantingTableBlock;
    }

    @Override
    public boolean shouldRender(EnchantingInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                Minecraft.getInstance().level != null &&
                info.readyToRender();
    }

    @Override
    protected void render(EnchantingInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeETable / info.getItemTransitionCountdown();

        if (!info.itemEnchantedCopy.isEmpty()) {
            for (int i = 0; i <= 2; i++) {
                EnchantingInfo.ETableInfo enchInfo =
                        i == 0 ? info.weakInfo : i == 1 ? info.midInfo : info.strongInfo;
                renderItem(info.itemEnchantedCopy, stack, info.getPosition(i + 1), itemSize,
                        getForwardFromPlayer(Minecraft.getInstance().player), info.getHitbox(i + 1), false);
                if (info.lookingAtIndex == i) {
                    if (enchInfo.isPresent()) {
                        renderText(new StringTextComponent(enchInfo.levelsNeeded + " (" + (i + 1) + ")"),
                                stack,
                                info.getPosition(i + 1).add(0, 0.33, 0));
                        renderText(enchInfo.textPreview,
                                stack,
                                info.getPosition(i + 1).add(0, -0.33, 0));
                    } else {
                        renderText(new StringTextComponent("No Enchantment!"),
                                stack, info.getPosition(i + 1).add(0, -0.2, 0));
                    }
                }

            }
        }
        if (info.items[0] != null && !info.items[0].isEmpty()) {
            renderItem(info.items[0], stack, info.getPosition(0), itemSize,
                    getForwardFromPlayer(Minecraft.getInstance().player), info.getHitbox(0), false);
        } else {
            renderHitbox(stack, info.getHitbox(0), info.getPosition(0));
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useETableImmersion;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo infoRaw, ImmersiveStorage storage) {
        EnchantingInfo info = (EnchantingInfo) infoRaw;
        info.items[0] = storage.items[0];
        if (storage.items[0] != null && !storage.items[0].isEmpty()) {
            info.itemEnchantedCopy = storage.items[0].copy();
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
    protected boolean slotShouldRenderHelpHitbox(EnchantingInfo info, int slotNum) {
        return info.items[0] == null || info.items[0].isEmpty();
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    public void trackObject(BlockPos pos) {
        for (EnchantingInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderETable);
                return;
            }
        }
        this.noInfosCooldown = 0;
        infos.add(new EnchantingInfo(pos, ClientConstants.ticksToRenderETable));
    }

    protected Vector3d getYDiffFromOffset(EnchantingInfo info, int slot) {
        return new Vector3d(0, this.yOffsets[info.yOffsetPositions[slot]], 0);
    }

    @Override
    protected void initInfo(EnchantingInfo info) {
        // NOOP: Changes based on the items moving up and down to look cool
    }
}
