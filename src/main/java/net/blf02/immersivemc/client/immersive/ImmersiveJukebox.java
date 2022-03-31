package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.JukeboxInfo;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveJukebox extends AbstractTileEntityImmersive<JukeboxTileEntity, JukeboxInfo> {
    
    protected static ImmersiveJukebox singleton = new ImmersiveJukebox();

    public static ImmersiveJukebox getSingleton() {
        return singleton;
    }

    @Override
    public void tick(JukeboxInfo info, boolean isInVR) {
        super.tick(info, isInVR);

        Vector3d topCenter = getTopCenterOfBlock(info.getTileEntity().getBlockPos());
        info.setPosition(0, topCenter);
        // North and south
        AxisAlignedBB hitbox = createHitbox(topCenter, 1f/16f);
        hitbox = hitbox.inflate(0, 0, 0.25); // Rectangular hitbox that covers disc slot
        info.setHitbox(0, hitbox);

    }

    @Override
    protected void render(JukeboxInfo info, MatrixStack stack, boolean isInVR) {
        renderHitbox(stack, info.getHibtox(0), info.getPosition(0));
    }

    @Override
    public JukeboxInfo getNewInfo(JukeboxTileEntity tileEnt) {
        return new JukeboxInfo(tileEnt, ClientConfig.ticksToHandleJukebox);
    }

    @Override
    public int getTickTime() {
        return ClientConfig.ticksToHandleJukebox;
    }

    @Override
    public boolean shouldRender(JukeboxInfo info, boolean isInVR) {
        return info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.getTileEntity().getBlockPos().relative(Direction.UP)).isAir() &&
                info.readyToRender() &&
                isInVR;
    }
}
