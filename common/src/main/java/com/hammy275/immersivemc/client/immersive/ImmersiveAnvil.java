package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.AnvilInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.storage.AnvilStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveAnvil extends AbstractWorldStorageImmersive<AnvilInfo> {

    protected final double dist = 1d/3d;
    public ImmersiveAnvil() {
        super(1);
    }

    @Override
    protected void initInfo(AnvilInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(AnvilInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);

        BlockState anvil = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        Direction facing = anvil.getValue(AnvilBlock.FACING); // "faces" long way towards the right
        info.renderDirection = facing.getClockWise();

        Vec3i temp = facing.getOpposite().getNormal();
        Vec3 facingOppositeNormal = new Vec3(temp.getX(), temp.getY(), temp.getZ());
        temp = facing.getNormal();
        Vec3 facingNormal = new Vec3(temp.getX(), temp.getY(), temp.getZ());

        Vec3 middle = getTopCenterOfBlock(info.getBlockPosition());
        Vec3 left = middle.add(facingOppositeNormal.multiply(dist, dist, dist));
        Vec3 right = middle.add(facingNormal.multiply(dist, dist, dist));

        info.setPosition(0, left);
        info.setPosition(1, middle);
        info.setPosition(2, right);

        info.textPos = info.getPosition(1).add(0, 0.5, 0);

        float hitboxSize = ClientConstants.itemScaleSizeAnvil / 2.05f;

        info.setHitbox(0, createHitbox(left, hitboxSize));
        info.setHitbox(1, createHitbox(middle, hitboxSize));
        info.setHitbox(2, createHitbox(right, hitboxSize));

        info.lastDir = facing;
    }

    @Override
    protected void doTick(AnvilInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);
        BlockState anvil = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        Direction facing = anvil.getValue(AnvilBlock.FACING); // "faces" long way towards the right


        if (facing != info.lastDir) {
            setHitboxes(info);
        }

    }

    @Override
    public BlockPos getLightPos(AnvilInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public boolean shouldRender(AnvilInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).canBeReplaced()
                && info.readyToRender();

    }

    @Override
    protected void render(AnvilInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeAnvil / info.getItemTransitionCountdown();

        // Render experience levels needed if we have an experience cost to show
        if (info.anvilCost > 0) {
            renderText(Component.literal(I18n.get("immersivemc.immersive.anvil.levels_needed", info.anvilCost)), stack,
                    info.textPos, info.light);
        }

        // Render the actual items
        for (int i = 0; i <= 2; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            renderItem(info.items[i], stack, info.getPosition(i),
                    renderSize, info.renderDirection, Direction.UP, info.getHitbox(i), false, -1, info.light);
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useAnvilImmersion;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo wInfo, ImmersiveStorage storageIn) {
        AnvilInfo info = (AnvilInfo) wInfo;
        info.items = storageIn.getItemsRaw();
        AnvilStorage aStorage = (AnvilStorage) storageIn;
        info.anvilCost = aStorage.xpLevels;
    }

    @Override
    public AnvilInfo getNewInfo(BlockPos pos) {
        return new AnvilInfo(pos, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderAnvil;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(AnvilInfo info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isAnvil(pos, state, tileEntity, level);
    }

    @Override
    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (AnvilInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderAnvil);
                return;
            }
        }
        infos.add(new AnvilInfo(pos, ClientConstants.ticksToRenderAnvil));
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }
}
