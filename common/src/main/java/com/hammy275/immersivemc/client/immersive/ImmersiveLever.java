package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.info.HitboxItemPair;
import com.hammy275.immersivemc.client.immersive.info.LeverInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.UsePacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ImmersiveLever extends AbstractImmersiveV2<LeverInfo, NullStorage> {
    public ImmersiveLever() {
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return null;
    }

    @Override
    public boolean shouldDisableRightClicksWhenInteractionsDisabled(LeverInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(LeverInfo info, NullStorage storage) {
        // NO-OP. No storage.
    }

    @Override
    public LeverInfo buildInfo(BlockPos pos, Level level) {
        LeverInfo info = new LeverInfo(pos);
        BlockState state = level.getBlockState(info.getBlockPosition());
        Vec3 center = Vec3.atCenterOf(info.getBlockPosition());
        AttachFace attachFace = state.getValue(BlockStateProperties.ATTACH_FACE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction towardsBaseDir;
        Direction towardsOnDir;
        switch (attachFace) {
            case WALL -> {
                towardsBaseDir = facing.getOpposite();
                towardsOnDir = Direction.DOWN;
            }
            case CEILING -> {
                towardsBaseDir = Direction.UP;
                towardsOnDir = facing;
            }
            case FLOOR -> {
                towardsBaseDir = Direction.DOWN;
                towardsOnDir = facing;
            }
            default -> throw new IllegalStateException("Lever is attached to unknown face " + attachFace.getSerializedName());
        }
        Vec3 towardsBase = Vec3.atLowerCornerOf(towardsBaseDir.getNormal());
        Vec3 towardsOn = Vec3.atLowerCornerOf(towardsOnDir.getNormal());
        center = center.add(towardsBase.scale(0.25));

        Vec3 offPos = center.add(towardsOn.scale(-0.25));
        Vec3 onPos = center.add(towardsOn.scale(0.25));

        info.getAllHitboxes().get(0).box = AABB.ofSize(offPos, 0.5, 0.5, 0.5);
        info.getAllHitboxes().get(1).box = AABB.ofSize(onPos, 0.5, 0.5, 0.5);

        return info;
    }

    @Override
    public int handleHitboxInteract(LeverInfo info, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        // NO-OP. Handled in doTick()
        return -1;
    }

    @Override
    public boolean shouldRender(LeverInfo info) {
        return true;
    }

    @Override
    public void render(LeverInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        for (int i = 0; i < 2; i++) {
            helpers.renderHitbox(stack, info.getAllHitboxes().get(i).box);
        }
    }

    @Override
    public void tick(LeverInfo info) {
        super.tick(info);
        BlockState lever = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        boolean powered = lever.getValue(BlockStateProperties.POWERED);
        int startHitbox = powered ? 1 : 0;
        int endHitbox = powered ? 0 : 1;

        for (int c = 0; c <= 1; c++) {
            IVRData hand = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c);
            int lastGrabbed = info.grabbedBox[c];
            int grabbed = Util.getFirstIntersect(hand.position(), info.getAllHitboxes().stream().map(HitboxItemPair::getHitbox).toList()).orElse(-1);
            info.grabbedBox[c] = grabbed;
            if (grabbed == endHitbox && lastGrabbed == startHitbox) {
                Util.useLever(Minecraft.getInstance().player, info.getBlockPosition());
                Network.INSTANCE.sendToServer(new UsePacket(info.getBlockPosition()));
            }
        }
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.leverHandler;
    }
}
