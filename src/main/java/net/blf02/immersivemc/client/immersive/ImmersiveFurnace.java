package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

public class ImmersiveFurnace extends AbstractBlockEntityImmersive<AbstractFurnaceBlockEntity, ImmersiveFurnaceInfo> {

    // We don't ever expect this to get too big (since this mod runs on clients separately)

    protected static final ImmersiveFurnace immersiveFurnace = new ImmersiveFurnace();

    public ImmersiveFurnace() {
        super(4);
    }

    public static ImmersiveFurnace getSingleton() {
        return immersiveFurnace;
    }

    @Override
    public ImmersiveFurnaceInfo getNewInfo(AbstractFurnaceBlockEntity tileEnt) {
        return new ImmersiveFurnaceInfo(tileEnt, ClientConstants.ticksToRenderFurnace);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderFurnace;
    }

    @Override
    public boolean hasValidBlock(ImmersiveFurnaceInfo info, Level level) {
        return level.getBlockEntity(info.getBlockPosition()) instanceof AbstractFurnaceBlockEntity;
    }

    @Override
    public boolean shouldRender(ImmersiveFurnaceInfo info, boolean isInVR) {
        Direction forward = info.forward;
        return forward != null && info.getBlockEntity().getLevel() != null &&
                info.getBlockEntity().getLevel().getBlockState(info.getBlockEntity().getBlockPos().relative(forward)).isAir()
                && info.readyToRender();
    }

    @Override
    protected void doTick(ImmersiveFurnaceInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        AbstractFurnaceBlockEntity furnace = info.getBlockEntity();
        Direction forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
        Vec3 pos = getDirectlyInFront(forward, furnace.getBlockPos());

        // Gets the offset on the x and z axis that the items should be placed in front of the furnace
        Direction left = getLeftOfDirection(forward);
        Vec3 toSmeltAndFuelOffset = new Vec3(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vec3 outputOffset = new Vec3(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        Vec3 posToSmelt;
        Vec3 posFuel;
        Vec3 posOutput;
        if (ActiveConfig.autoCenterFurnace) {
            posFuel = pos.add(left.getNormal().getX() * 0.5, 0.25, left.getNormal().getZ() * 0.5);
            if (info.items[2] == null || info.items[2].isEmpty()) {
                posToSmelt = posFuel.add(0, 0.5, 0);
                posOutput = null;
            } else if (info.items[0] == null || info.items[0].isEmpty()) {
                posOutput = posFuel.add(0, 0.5, 0);
                posToSmelt = null; // If this is null, we need to handle right clicks "smartly"
            } else {
                posToSmelt = pos.add(toSmeltAndFuelOffset).add(0, 0.75, 0);
                posOutput = pos.add(outputOffset).add(0, 0.75, 0);
            }
        } else {
            posToSmelt = pos.add(0, 0.75, 0).add(toSmeltAndFuelOffset);
            posFuel = pos.add(0, 0.25, 0).add(toSmeltAndFuelOffset);
            posOutput = pos.add(0, 0.5, 0).add(outputOffset);
        }
        info.setPosition(0, posToSmelt);
        info.setPosition(1, posFuel);
        info.setPosition(2, posOutput);

        // Set hitboxes for logic to use
        if (posToSmelt != null) {
            info.setHitbox(0, createHitbox(posToSmelt, ClientConstants.itemScaleSizeFurnace / 3.0f));
        } else {
            info.setHitbox(0, null);
        }
        info.setHitbox(1, createHitbox(posFuel, ClientConstants.itemScaleSizeFurnace / 3.0f));
        if (posOutput != null) {
            info.setHitbox(2, createHitbox(posOutput, ClientConstants.itemScaleSizeFurnace / 3.0f));
        } else {
            info.setHitbox(2, null);
        }

    }

    protected void render(ImmersiveFurnaceInfo info, PoseStack stack, boolean isInVR) {
        float size = ClientConstants.itemScaleSizeFurnace / info.getItemTransitionCountdown();

        // Render all of the items

        renderItem(info.items[0], stack, info.getPosition(0), size, info.forward, info.getHitbox(0), true);
        renderItem(info.items[1], stack, info.getPosition(1), size, info.forward, info.getHitbox(1), true);
        if (info.items[2] != null && !info.items[2].isEmpty()) {
            // If empty, we don't need to render, AND it might be null because of autoCenterFurnace
            renderItem(info.items[2], stack, info.getPosition(2), size, info.forward, info.getHitbox(2), true);
        }

    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useFurnaceImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int slot, InteractionHand hand) {
        ImmersiveFurnaceInfo infoF = (ImmersiveFurnaceInfo) info;
        if (info.getPosition(0) == null && slot == 2) { // We're right-clicking on the output slot, but there's no input slot
            ItemStack handItem = player.getItemInHand(hand);
            if (!handItem.isEmpty() &&
                    (!Util.stacksEqualBesidesCount(handItem, infoF.items[2]) || handItem.getCount() == handItem.getMaxStackSize())) {
                // If we don't have an input slot, set to the input slot instead of output if:
                // Our hand is NOT empty (we have something to put in) AND
                // We're holding a different item than what's in the output OR what we have in our hand can't be added to
                slot = 0;
            }
        }
        Network.INSTANCE.sendToServer(new SwapPacket(
                infoF.getBlockEntity().getBlockPos(), slot, hand
        ));
    }

    @Override
    protected void initInfo(ImmersiveFurnaceInfo info) {
        // NOOP: Some hitboxes and positions can change if autoCenterFurnace is on
    }
}
