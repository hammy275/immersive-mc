package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveFurnace extends AbstractImmersive<AbstractFurnaceTileEntity, ImmersiveFurnaceInfo> {

    // We don't ever expect this to get too big (since this mod runs on clients separately)

    protected static final ImmersiveFurnace immersiveFurnace = new ImmersiveFurnace();

    public static ImmersiveFurnace getSingleton() {
        return immersiveFurnace;
    }

    @Override
    public ImmersiveFurnaceInfo getNewInfo(AbstractFurnaceTileEntity tileEnt) {
        return new ImmersiveFurnaceInfo(tileEnt, ClientConfig.ticksToRenderFurnace);
    }

    public void handleImmersion(ImmersiveFurnaceInfo info, MatrixStack stack) {
        super.handleImmersion(info, stack);
        AbstractFurnaceTileEntity furnace = info.getTileEntity();
        Direction forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
        Vector3d pos = getDirectlyInFront(forward, furnace.getBlockPos());


        // Gets the offset on the x and z axis that the items should be placed in front of the furnace
        Direction left = getLeftOfDirection(forward);
        Vector3d toSmeltAndFuelOffset = new Vector3d(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vector3d outputOffset = new Vector3d(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        ItemStack toSmelt = furnace.getItem(0);
        ItemStack fuel = furnace.getItem(1);
        ItemStack output = furnace.getItem(2);

        float size = ClientConfig.itemScaleSize / info.getCountdown();

        // Render all of the items
        Vector3d posToSmelt = pos.add(0, 0.75, 0).add(toSmeltAndFuelOffset);
        renderItem(toSmelt, stack, posToSmelt, size, forward, info.getHibtox(0));
        Vector3d posFuel = pos.add(0, 0.25, 0).add(toSmeltAndFuelOffset);
        renderItem(fuel, stack, posFuel, size, forward, info.getHibtox(1));
        Vector3d posOutput = pos.add(0, 0.5, 0).add(outputOffset);
        renderItem(output, stack, posOutput, size, forward, info.getHibtox(2));

        // Set hitboxes for logic to use
        info.setHitbox(0, new AxisAlignedBB(
                posToSmelt.x - ClientConfig.itemScaleSize / 3.0,
                posToSmelt.y - ClientConfig.itemScaleSize / 3.0,
                posToSmelt.z - ClientConfig.itemScaleSize / 3.0,
                posToSmelt.x + ClientConfig.itemScaleSize / 3.0,
                posToSmelt.y + ClientConfig.itemScaleSize / 3.0,
                posToSmelt.z + ClientConfig.itemScaleSize / 3.0));

        info.setHitbox(1, new AxisAlignedBB(
                posFuel.x - ClientConfig.itemScaleSize / 3.0,
                posFuel.y - ClientConfig.itemScaleSize / 3.0,
                posFuel.z - ClientConfig.itemScaleSize / 3.0,
                posFuel.x + ClientConfig.itemScaleSize / 3.0,
                posFuel.y + ClientConfig.itemScaleSize / 3.0,
                posFuel.z + ClientConfig.itemScaleSize / 3.0));

        info.setHitbox(2, new AxisAlignedBB(
                posOutput.x - ClientConfig.itemScaleSize / 3.0,
                posOutput.y - ClientConfig.itemScaleSize / 3.0,
                posOutput.z - ClientConfig.itemScaleSize / 3.0,
                posOutput.x + ClientConfig.itemScaleSize / 3.0,
                posOutput.y + ClientConfig.itemScaleSize / 3.0,
                posOutput.z + ClientConfig.itemScaleSize / 3.0));
    }

}
