package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.client.immersive.info.SmithingTableInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveSmithingTable extends AbstractWorldStorageImmersive<SmithingTableInfo> {

    protected final double dist = 1d/3d;
    protected final float extraOutputSize = 1.5f;
    public ImmersiveSmithingTable() {
        super(1);
    }

    @Override
    protected void initInfo(SmithingTableInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(SmithingTableInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);

        Direction facing = getForwardFromPlayer(Minecraft.getInstance().player).getCounterClockWise();
        info.renderDirection = facing.getClockWise();

        Vec3i temp = facing.getOpposite().getNormal();
        Vec3 facingOppositeNormal = new Vec3(temp.getX(), temp.getY(), temp.getZ());
        temp = facing.getNormal();
        Vec3 facingNormal = new Vec3(temp.getX(), temp.getY(), temp.getZ());

        Vec3 middle = getTopCenterOfBlock(info.getBlockPosition());
        Vec3 left = middle.add(facingOppositeNormal.multiply(dist, dist, dist));
        Vec3 right = middle.add(facingNormal.multiply(dist, dist, dist));
        Vec3 above = middle.add(0, 0.5, 0);

        info.setPosition(0, left);
        info.setPosition(1, middle);
        info.setPosition(2, right);
        info.setPosition(3, above);

        float hitboxSize = ClientConstants.itemScaleSizeSmithingTable / 2.05f;

        info.setHitbox(0, createHitbox(left, hitboxSize));
        info.setHitbox(1, createHitbox(middle, hitboxSize));
        info.setHitbox(2, createHitbox(right, hitboxSize));
        info.setHitbox(3, createHitbox(above, hitboxSize * extraOutputSize));

        info.lastDir = facing;
    }

    @Override
    protected void doTick(SmithingTableInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);
        Direction facing = getForwardFromPlayer(Minecraft.getInstance().player).getCounterClockWise();


        if (facing != info.lastDir) {
            setHitboxes(info);
        }

    }

    @Override
    public BlockPos getLightPos(SmithingTableInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public boolean shouldRender(SmithingTableInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).canBeReplaced()
                && info.readyToRender();

    }

    @Override
    protected void render(SmithingTableInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeSmithingTable / info.getItemTransitionCountdown();

        for (int i = 0; i <= 2; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            renderItem(info.items[i], stack, info.getPosition(i),
                    renderSize, info.renderDirection, Direction.UP, info.getHitbox(i), true, -1, info.light);
        }
        int degreesRotation = (int) (info.ticksActive % 100d * 3.6d);
        renderItem(info.items[3], stack, info.getTriggerHitbox(0).getCenter(),
                itemSize * extraOutputSize, info.renderDirection, null, info.getTriggerHitbox(0), true,
                ActiveConfig.spinCraftingOutput ? degreesRotation : -1, info.light);
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useSmithingTableImmersion;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo wInfo, ImmersiveStorage storageIn) {
        SmithingTableInfo info = (SmithingTableInfo) wInfo;
        info.items = storageIn.getItemsRaw();
    }

    @Override
    public SmithingTableInfo getNewInfo(BlockPos pos) {
        return new SmithingTableInfo(pos, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderSmithingTable;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(SmithingTableInfo info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isSmithingTable(pos, state, tileEntity, level);
    }

    @Override
    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (SmithingTableInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderSmithingTable);
                return;
            }
        }
        infos.add(new SmithingTableInfo(pos, ClientConstants.ticksToRenderSmithingTable));
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        handleRightClick((SmithingTableInfo) info, player, 3, InteractionHand.MAIN_HAND);
    }
}
