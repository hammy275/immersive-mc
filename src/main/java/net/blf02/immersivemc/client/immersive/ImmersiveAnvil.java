package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AnvilInfo;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Objects;

public class ImmersiveAnvil extends AbstractImmersive<AnvilInfo> {

    public static final ImmersiveAnvil singleton = new ImmersiveAnvil();

    protected final double dist = 1d/3d;

    public ImmersiveAnvil() {
        super(-1); // All client side, no need to have a maximum immersive count
    }

    @Override
    protected void doTick(AnvilInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);

        if (info.anvilPos != null &&
                Minecraft.getInstance().player.distanceToSqr(Vector3d.atCenterOf(info.anvilPos)) >
                        CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }

        BlockState anvil = Minecraft.getInstance().level.getBlockState(info.anvilPos);
        info.isReallyAnvil = isAnvil(anvil);
        Direction facing;
        if (info.isReallyAnvil) {
            facing = anvil.getValue(AnvilBlock.FACING); // "faces" long way towards the right
        } else {
            facing = getForwardFromPlayer(Minecraft.getInstance().player).getCounterClockWise();
        }
        info.renderDirection = facing.getClockWise();

        Vector3i temp = facing.getOpposite().getNormal();
        Vector3d facingOppositeNormal = new Vector3d(temp.getX(), temp.getY(), temp.getZ());
        temp = facing.getNormal();
        Vector3d facingNormal = new Vector3d(temp.getX(), temp.getY(), temp.getZ());

        Vector3d middle = getTopCenterOfBlock(info.anvilPos);
        Vector3d left = middle.add(facingOppositeNormal.multiply(dist, dist, dist));
        Vector3d right = middle.add(facingNormal.multiply(dist, dist, dist));

        info.setPosition(0, left);
        info.setPosition(1, middle);
        info.setPosition(2, right);

        info.textPos = info.getPosition(1).add(0, 0.5, 0);

        float hitboxSize = ClientConstants.itemScaleSizeAnvil / 2f;

        info.setHitbox(0, createHitbox(left, hitboxSize));
        info.setHitbox(1, createHitbox(middle, hitboxSize));
        info.setHitbox(2, createHitbox(right, hitboxSize));

    }

    @Override
    public boolean shouldRender(AnvilInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.anvilPos.above()).isAir()
                && info.readyToRender();

    }

    @Override
    protected void render(AnvilInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeAnvil / info.getCountdown();

        // Render experience levels needed if we have an experience cost to show
        if (info.isReallyAnvil && ClientStorage.anvilCost > 0) {
            renderText(new StringTextComponent("Levels Needed: " + ClientStorage.anvilCost), stack,
                    info.textPos);
        }

        // Render the actual items
        for (int i = 0; i <= 2; i++) {
            ItemStack item = info.isReallyAnvil ? ClientStorage.anvilStorage[i] : ClientStorage.smithingStorage[i];
            renderItem(item, stack, info.getPosition(i),
                    itemSize, info.renderDirection, Direction.UP, info.getHibtox(i), false);
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useAnvilImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        AnvilInfo infoA = (AnvilInfo) info;
        ClientSwap.anvilSwap(closest, hand, infoA.anvilPos);
    }

    public void trackObject(BlockPos pos) {
        for (AnvilInfo info : getTrackedObjects()) {
            if (info.anvilPos.equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderAnvil);
                return;
            }
        }
        infos.add(new AnvilInfo(pos, ClientConstants.ticksToRenderAnvil));
    }

    protected boolean isAnvil(BlockState state) {
        return state.getBlock() instanceof AnvilBlock;
    }
}
