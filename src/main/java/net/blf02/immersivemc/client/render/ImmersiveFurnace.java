package net.blf02.immersivemc.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.common.config.ClientConfig;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.LinkedList;
import java.util.List;

public class ImmersiveFurnace {

    // We don't ever expect this to get too big (since this mod runs on clients separately)
    public static List<ImmersiveFurnaceInfo> furnaces = new LinkedList<>();

    public static final Vector3d toSmeltOffset = new Vector3d(0, 0.75, 0.25);

    private static final ItemStack fireStack = new ItemStack(Blocks.FIRE);

    public static void trackFurnace(AbstractFurnaceTileEntity furnace) {
        for (ImmersiveFurnaceInfo info : furnaces) {
            if (info.furnace == furnace) {
                info.ticksLeft = ClientConfig.ticksToRenderFurnace;
                return;
            }
        }
        furnaces.add(new ImmersiveFurnaceInfo(furnace, ClientConfig.ticksToRenderFurnace));
    }

    public static Direction getLeftOfDirection(Direction forward) {
        /**
         * Gets the direction to the left from the Direction's perspective, assuming the Direction is
         * looking at the player. This makes it to the right for the player.
         */
        if (forward == Direction.UP || forward == Direction.DOWN) {
            throw new IllegalArgumentException("Direction cannot be up or down!");
        }
        if (forward == Direction.NORTH) {
            return Direction.WEST;
        } else if (forward == Direction.WEST) {
            return Direction.SOUTH;
        } else if (forward == Direction.SOUTH) {
            return Direction.EAST;
        }
        return Direction.NORTH;
    }

    public static void handleFurnace(ImmersiveFurnaceInfo info, MatrixStack stack) {
        AbstractFurnaceTileEntity furnace = info.furnace;
        Direction forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
        Vector3d pos;
        // This mess sets pos to always be directly in front of the face of the furnace
        if (forward == Direction.SOUTH) {
            BlockPos front = furnace.getBlockPos().relative(forward);
            pos = new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forward == Direction.WEST) {
            BlockPos front = furnace.getBlockPos();
            pos = new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forward == Direction.NORTH) {
            BlockPos front = furnace.getBlockPos().relative(Direction.EAST);
            pos = new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forward == Direction.EAST) {
            BlockPos front = furnace.getBlockPos().relative(Direction.SOUTH).relative(Direction.EAST);
            pos = new Vector3d(front.getX(), front.getY(), front.getZ());
        } else {
            throw new IllegalArgumentException("Furnaces can't point up or down?!?!");
        }

        Direction left = getLeftOfDirection(forward);
        Vector3d toSmeltAndFuelOffset = new Vector3d(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vector3d outputOffset = new Vector3d(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        ItemStack toSmelt = furnace.getItem(0);
        ItemStack fuel = furnace.getItem(1);
        ItemStack output = furnace.getItem(2);

        Vector3d posToSmelt = pos.add(0, 0.75, 0).add(toSmeltAndFuelOffset);
        renderItem(toSmelt, stack, posToSmelt);
        Vector3d posFuel = pos.add(0, 0.25, 0).add(toSmeltAndFuelOffset);
        renderItem(fuel, stack, posFuel);
        Vector3d posOutput = pos.add(0, 0.5, 0).add(outputOffset);
        renderItem(output, stack, posOutput);

        info.toSmeltHitbox = new AxisAlignedBB(
                posToSmelt.x - ClientConfig.itemScaleSize / 2.0,
                posToSmelt.y - ClientConfig.itemScaleSize / 2.0,
                posToSmelt.z - ClientConfig.itemScaleSize / 2.0,
                posToSmelt.x + ClientConfig.itemScaleSize / 2.0,
                posToSmelt.y + ClientConfig.itemScaleSize / 2.0,
                posToSmelt.z + ClientConfig.itemScaleSize / 2.0);

        info.fuelHitbox = new AxisAlignedBB(
                posFuel.x - ClientConfig.itemScaleSize / 2.0,
                posFuel.y - ClientConfig.itemScaleSize / 2.0,
                posFuel.z - ClientConfig.itemScaleSize / 2.0,
                posFuel.x + ClientConfig.itemScaleSize / 2.0,
                posFuel.y + ClientConfig.itemScaleSize / 2.0,
                posFuel.z + ClientConfig.itemScaleSize / 2.0);

        info.outputHitbox = new AxisAlignedBB(
                posOutput.x - ClientConfig.itemScaleSize / 2.0,
                posOutput.y - ClientConfig.itemScaleSize / 2.0,
                posOutput.z - ClientConfig.itemScaleSize / 2.0,
                posOutput.x + ClientConfig.itemScaleSize / 2.0,
                posOutput.y + ClientConfig.itemScaleSize / 2.0,
                posOutput.z + ClientConfig.itemScaleSize / 2.0);
    }

    public static void renderItem(ItemStack item, MatrixStack stack, Vector3d pos) {
        if (item != ItemStack.EMPTY) {
            stack.pushPose();

            ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);

            stack.scale(ClientConfig.itemScaleSize, ClientConfig.itemScaleSize, ClientConfig.itemScaleSize);

            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemCameraTransforms.TransformType.FIXED,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    stack, Minecraft.getInstance().renderBuffers().bufferSource());

            stack.popPose();
        }
    }

    public static class ImmersiveFurnaceInfo {

        public final AbstractFurnaceTileEntity furnace;
        public int ticksLeft;
        public AxisAlignedBB toSmeltHitbox = null;
        public AxisAlignedBB fuelHitbox = null;
        public AxisAlignedBB outputHitbox = null;

        public ImmersiveFurnaceInfo(AbstractFurnaceTileEntity furnace, int ticksLeft) {
            this.furnace = furnace;
            this.ticksLeft = ticksLeft;
        }

        public boolean hasHitboxes() {
            // If we have one hitbox, we have all 3
            return toSmeltHitbox != null;
        }
    }
}
