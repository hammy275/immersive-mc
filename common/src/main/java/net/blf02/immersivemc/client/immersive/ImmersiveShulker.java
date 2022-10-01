package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ShulkerInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.immersive.ImmersiveCheckers;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ImmersiveShulker extends AbstractBlockEntityImmersive<ShulkerBoxBlockEntity, ShulkerInfo> {
    public ImmersiveShulker() {
        super(4);
    }

    @Override
    public ShulkerInfo getNewInfo(BlockEntity tileEnt) {
        return new ShulkerInfo((ShulkerBoxBlockEntity) tileEnt, ClientConstants.ticksToRenderShulker);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderShulker;
    }

    @Override
    public boolean shouldRender(ShulkerInfo info, boolean isInVR) {
        return info.readyToRender() && (info.isOpen || true); // TODO: Remove true
    }

    @Override
    protected void render(ShulkerInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeShulker / info.getItemTransitionCountdown();
        for (int i = 0; i < 27; i++) {
            renderItem(info.items[i], stack, info.getPosition(i), itemSize,
                    getForwardFromPlayer(Minecraft.getInstance().player).getOpposite(), null,
                    info.getHitbox(i), true, -1);
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useShulkerImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isShulkerBox(pos, state, tileEntity, level);
    }

    @Override
    public AbstractImmersive<? extends AbstractImmersiveInfo> getSingleton() {
        return Immersives.immersiveShulker;
    }

    @Override
    protected void initInfo(ShulkerInfo info) {
        setHitboxes(info);
    }

    public void setHitboxes(ShulkerInfo info) {
        Vec3[] positions = get3x3VerticalGrid(info.getBlockPosition(), 0.124);
        for (int i = 0; i < 27; i++) {
            info.setHitbox(i, null);
            info.setPosition(i, null);
        }
        for (int i = 0; i < 9; i++) {
            positions[i] = positions[i].add(0, 0.3, 0); // Move up a bit for gap in Shulker box
            info.setHitbox(i + info.getRowNum() * 9, createHitbox(positions[i], 0.062f));
            info.setPosition(i + info.getRowNum() * 9, positions[i]);
        }

        info.lastDir = getForwardFromPlayer(Minecraft.getInstance().player);
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        if (((ShulkerInfo) info).isOpen || true) { // TODO: Remove true check
            Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), closest, hand));
        }

    }

    @Override
    protected void doTick(ShulkerInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (info.lastDir != getForwardFromPlayer(Minecraft.getInstance().player)) {
            setHitboxes(info);
        }
    }
}
