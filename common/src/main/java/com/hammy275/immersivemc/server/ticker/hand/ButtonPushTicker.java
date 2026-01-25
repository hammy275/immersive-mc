package com.hammy275.immersivemc.server.ticker.hand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.ticker.AbstractHandTicker;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.mixin.ButtonBlockMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

public class ButtonPushTicker extends AbstractHandTicker {

    @Override
    protected void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        BlockPos pos = BlockPos.containing(handData.getPos());
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() instanceof ButtonBlock && !state.getValue(ButtonBlock.POWERED)) {
            ButtonBlock button = (ButtonBlock) state.getBlock();
            VoxelShape shape = state.getShape(player.level, pos,
                    CollisionContext.of(player));
            // Start and end vectors need to be slightly different, so we just give a bit extra on the y axis
            BlockHitResult res = shape.clip(
                    handData.getPos().add(0, -0.01, 0),
                    handData.getPos().add(0, 0.01, 0),
                    pos);
            if (res != null && res.getBlockPos().equals(pos)) {
                button.press(state, player.level, pos);
                ((ButtonBlockMixin) button).immersiveMC$playButtonSound(null, player.level, pos, true);
                VRRumble.rumbleIfVR(player, hand, CommonConstants.vibrationTimeWorldInteraction);
            }
        }
    }

    @Override
    protected boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        return ActiveConfig.getConfigForPlayer(player).useButtonImmersive;
    }
}
