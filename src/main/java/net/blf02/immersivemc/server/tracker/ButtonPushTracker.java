package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.server.PlayerConfigs;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;

public class ButtonPushTracker extends AbstractTracker {

    public ButtonPushTracker() {
        ServerTrackerInit.playerTrackers.add(this);
    }

    @Override
    protected void tick(PlayerEntity player) {
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
        for (int i = 0; i <= 1; i++) {
            IVRData controller = vrPlayer.getController(i);
            BlockPos pos = new BlockPos(controller.position());
            BlockState state = player.level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractButtonBlock && !state.getValue(AbstractButtonBlock.POWERED)) {
                AbstractButtonBlock button = (AbstractButtonBlock) state.getBlock();
                VoxelShape shape = button.getShape(state, player.level, pos,
                        ISelectionContext.of(player));
                // Start and end vectors need to be slightly different, so we just give a bit extra on the y axis
                BlockRayTraceResult res = shape.clip(
                        controller.position().add(0, -0.01, 0),
                        controller.position().add(0, 0.01, 0),
                        pos);
                if (res != null && res.getBlockPos().equals(pos)) {
                    button.press(state, player.level, pos);
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(PlayerEntity player) {
        return ActiveConfig.useButton &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player) && VRPlugin.API.apiActive(player)
                && PlayerConfigs.getConfig(player).useButtons;
    }
}
