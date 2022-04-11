package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveFurnace extends AbstractTileEntityImmersive<AbstractFurnaceTileEntity, ImmersiveFurnaceInfo> {

    // We don't ever expect this to get too big (since this mod runs on clients separately)

    protected static final ImmersiveFurnace immersiveFurnace = new ImmersiveFurnace();

    public ImmersiveFurnace() {
        super(4);
    }

    public static ImmersiveFurnace getSingleton() {
        return immersiveFurnace;
    }

    @Override
    public ImmersiveFurnaceInfo getNewInfo(AbstractFurnaceTileEntity tileEnt) {
        return new ImmersiveFurnaceInfo(tileEnt, ClientConstants.ticksToRenderFurnace);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderFurnace;
    }

    @Override
    public boolean shouldRender(ImmersiveFurnaceInfo info, boolean isInVR) {
        Direction forward = info.forward;
        return forward != null && info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.getTileEntity().getBlockPos().relative(forward)).isAir()
                && info.readyToRender();
    }

    @Override
    protected void doTick(ImmersiveFurnaceInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        AbstractFurnaceTileEntity furnace = info.getTileEntity();
        Direction forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
        Vector3d pos = getDirectlyInFront(forward, furnace.getBlockPos());

        // Gets the offset on the x and z axis that the items should be placed in front of the furnace
        Direction left = getLeftOfDirection(forward);
        Vector3d toSmeltAndFuelOffset = new Vector3d(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vector3d outputOffset = new Vector3d(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        Vector3d posToSmelt = pos.add(0, 0.75, 0).add(toSmeltAndFuelOffset);
        info.setPosition(0, posToSmelt);
        Vector3d posFuel = pos.add(0, 0.25, 0).add(toSmeltAndFuelOffset);
        info.setPosition(1, posFuel);
        Vector3d posOutput = pos.add(0, 0.5, 0).add(outputOffset);
        info.setPosition(2, posOutput);

        // Set hitboxes for logic to use
        info.setHitbox(0, new AxisAlignedBB(
                posToSmelt.x - ClientConstants.itemScaleSizeFurnace / 3.0,
                posToSmelt.y - ClientConstants.itemScaleSizeFurnace / 3.0,
                posToSmelt.z - ClientConstants.itemScaleSizeFurnace / 3.0,
                posToSmelt.x + ClientConstants.itemScaleSizeFurnace / 3.0,
                posToSmelt.y + ClientConstants.itemScaleSizeFurnace / 3.0,
                posToSmelt.z + ClientConstants.itemScaleSizeFurnace / 3.0));

        info.setHitbox(1, new AxisAlignedBB(
                posFuel.x - ClientConstants.itemScaleSizeFurnace / 3.0,
                posFuel.y - ClientConstants.itemScaleSizeFurnace / 3.0,
                posFuel.z - ClientConstants.itemScaleSizeFurnace / 3.0,
                posFuel.x + ClientConstants.itemScaleSizeFurnace / 3.0,
                posFuel.y + ClientConstants.itemScaleSizeFurnace / 3.0,
                posFuel.z + ClientConstants.itemScaleSizeFurnace / 3.0));

        info.setHitbox(2, new AxisAlignedBB(
                posOutput.x - ClientConstants.itemScaleSizeFurnace / 3.0,
                posOutput.y - ClientConstants.itemScaleSizeFurnace / 3.0,
                posOutput.z - ClientConstants.itemScaleSizeFurnace / 3.0,
                posOutput.x + ClientConstants.itemScaleSizeFurnace / 3.0,
                posOutput.y + ClientConstants.itemScaleSizeFurnace / 3.0,
                posOutput.z + ClientConstants.itemScaleSizeFurnace / 3.0));
    }

    protected void render(ImmersiveFurnaceInfo info, MatrixStack stack, boolean isInVR) {
        float size = ClientConstants.itemScaleSizeFurnace / info.getCountdown();

        // Render all of the items

        renderItem(info.items[0], stack, info.getPosition(0), size, info.forward, info.getHibtox(0));
        renderItem(info.items[1], stack, info.getPosition(1), size, info.forward, info.getHibtox(1));
        renderItem(info.items[2], stack, info.getPosition(2), size, info.forward, info.getHibtox(2));


    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useFurnaceImmersion;
    }

}
