package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.EnchantingInfo;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.blf02.immersivemc.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImmersiveETable extends AbstractImmersive<EnchantingInfo> {

    protected final float[] yOffsets;

    public static final ImmersiveETable singleton = new ImmersiveETable();

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

        if (info.getBlockPosition() != null &&
                Minecraft.getInstance().player.distanceToSqr(Vector3d.atCenterOf(info.getBlockPosition())) >
                        CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }


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
                info.getHibtox(1), info.getHibtox(2), info.getHibtox(3));
        closest.ifPresent(targetSlot -> info.lookingAtIndex = targetSlot);
    }

    @Override
    public boolean shouldRender(EnchantingInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                Minecraft.getInstance().level != null &&
                info.readyToRender();
    }

    @Override
    protected void render(EnchantingInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeETable / info.getCountdown();

        if (!ClientStorage.eTableEnchCopy.isEmpty()) { // If one is active, all are
            for (int i = 0; i <= 2; i++) {
                ClientStorage.ETableInfo enchInfo =
                        i == 0 ? ClientStorage.weakInfo : i == 1 ? ClientStorage.midInfo : ClientStorage.strongInfo;
                renderItem(ClientStorage.eTableEnchCopy, stack, info.getPosition(i + 1), itemSize,
                        getForwardFromPlayer(Minecraft.getInstance().player), info.getHibtox(i + 1), false);
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
        if (!ClientStorage.eTableItem.isEmpty()) {
            renderItem(ClientStorage.eTableItem, stack, info.getPosition(0), itemSize,
                    getForwardFromPlayer(Minecraft.getInstance().player), info.getHibtox(0), false);
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useETableImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        ClientSwap.eTableSwap(closest, hand, info.getBlockPosition());
    }

    public void trackObject(BlockPos pos) {
        for (EnchantingInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderETable);
                return;
            }
        }
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
