package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.mixin.ButtonBlockMixin;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ButtonPushTracker extends AbstractTracker {

    public ButtonPushTracker() {
        ServerTrackerInit.playerTrackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
        for (int i = 0; i <= 1; i++) {
            IVRData controller = vrPlayer.getController(i);
            BlockPos pos = BlockPos.containing(controller.position());
            BlockState state = player.level.getBlockState(pos);
            if (state.getBlock() instanceof ButtonBlock && !state.getValue(ButtonBlock.POWERED)) {
                ButtonBlock button = (ButtonBlock) state.getBlock();
                VoxelShape shape = button.getShape(state, player.level, pos,
                        CollisionContext.of(player));
                // Start and end vectors need to be slightly different, so we just give a bit extra on the y axis
                BlockHitResult res = shape.clip(
                        controller.position().add(0, -0.01, 0),
                        controller.position().add(0, 0.01, 0),
                        pos);
                if (res != null && res.getBlockPos().equals(pos)) {
                    button.press(state, player.level, pos);
                    ((ButtonBlockMixin) button).playButtonSound(null, player.level, pos, true);
                    VRRumble.rumbleIfVR(player, i, CommonConstants.vibrationTimeWorldInteraction);
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        return ActiveConfig.FILE_SERVER.useButtonImmersive &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player) && VRPlugin.API.apiActive(player)
                && ActiveConfig.getConfigForPlayer(player).useButtonImmersive;
    }
}
