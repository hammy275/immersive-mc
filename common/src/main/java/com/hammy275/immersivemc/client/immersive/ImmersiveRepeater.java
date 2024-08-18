package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.HitboxItemPair;
import com.hammy275.immersivemc.client.immersive.info.RepeaterInfo;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SetRepeaterPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ImmersiveRepeater extends AbstractImmersive<RepeaterInfo, NullStorage> {

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("repeater", () -> new ItemStack(Items.REPEATER), ImmersiveMCConfig.useRepeaterImmersion);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(RepeaterInfo info) {
        return VRPluginVerify.clientInVR();
    }

    @Override
    public void processStorageFromNetwork(RepeaterInfo info, NullStorage storage) {
        // NO-OP. Repeaters don't receive from the server.
    }

    @Override
    public RepeaterInfo buildInfo(BlockPos pos, Level level) {
        RepeaterInfo info = new RepeaterInfo(pos);
        Objects.requireNonNull(Minecraft.getInstance().level);
        BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Direction forwardDir = facing.getOpposite();
        Vec3 forward = Vec3.atLowerCornerOf(forwardDir.getNormal());
        Vec3 centerPos = Vec3.upFromBottomCenterOf(info.getBlockPosition(), 1).add(0, -0.675, 0);

        info.getAllHitboxes().get(0).box = AABB.ofSize(centerPos.add(forward.multiply(1d/16d, 0, 1d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0);
        info.getAllHitboxes().get(1).box = AABB.ofSize(centerPos.add(forward.multiply(-1d/16d, 0, -1d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0);
        info.getAllHitboxes().get(2).box = AABB.ofSize(centerPos.add(forward.multiply(-3d/16d, 0, -3d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0);
        info.getAllHitboxes().get(3).box = AABB.ofSize(centerPos.add(forward.multiply(-5d/16d, 0, -5d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0);

        return info;
    }

    @Override
    public int handleHitboxInteract(RepeaterInfo info, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        return 0;
    }

    @Override
    public boolean shouldRender(RepeaterInfo info) {
        if (Minecraft.getInstance().player == null) return false;
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).getMaterial().isReplaceable();
    }

    @Override
    public void render(RepeaterInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        for (int i = 0; i <= 3; i++) {
            helpers.renderHitbox(stack, info.getAllHitboxes().get(i).box);
        }
    }

    @Override
    public void tick(RepeaterInfo info) {
        super.tick(info);

        if (VRPluginVerify.clientInVR()) {
            BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
            for (int c = 0; c <= 1; c++) {
                Vec3 pos = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c).position();
                Optional<Integer> hit = Util.getClosestIntersect(pos, info.getAllHitboxes().stream().map(HitboxItemPair::getHitbox).toList());
                int repeaterValue = state.getValue(RepeaterBlock.DELAY);
                if (hit.isPresent()) {
                    int delayHit = hit.get() + 1;
                    if (delayHit == repeaterValue) {
                        info.grabbedCurrent[c] = true;
                    } else if (info.grabbedCurrent[c]) {
                        Util.setRepeater(Minecraft.getInstance().level, info.getBlockPosition(), delayHit);
                        Network.INSTANCE.sendToServer(new SetRepeaterPacket(info.getBlockPosition(), delayHit));
                    }
                } else {
                    info.grabbedCurrent[c] = false;
                }
            }
        }
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.repeaterHandler;
    }
}
