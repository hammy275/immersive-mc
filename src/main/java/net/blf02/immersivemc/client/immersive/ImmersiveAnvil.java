package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.blf02.immersivemc.client.immersive.info.AnvilInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.InteractPacket;
import net.blf02.immersivemc.common.storage.AnvilStorage;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Objects;

public class ImmersiveAnvil extends AbstractWorldStorageImmersive<AnvilInfo> {

    public static final ImmersiveAnvil singleton = new ImmersiveAnvil();

    protected final double dist = 1d/3d;
    public ImmersiveAnvil() {
        super(1);
    }

    @Override
    protected void initInfo(AnvilInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(AnvilInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);

        BlockState anvil = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
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

        Vector3d middle = getTopCenterOfBlock(info.getBlockPosition());
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

        info.lastDir = facing;
    }

    @Override
    protected void doTick(AnvilInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().level);
        Direction facing;
        if (info.isReallyAnvil) {
            BlockState anvil = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
            facing = anvil.getValue(AnvilBlock.FACING); // "faces" long way towards the right
        } else {
            facing = getForwardFromPlayer(Minecraft.getInstance().player).getCounterClockWise();
        }

        if (facing != info.lastDir) {
            setHitboxes(info);
        }

    }

    @Override
    public boolean hasValidBlock(AnvilInfo info, World level) {
        BlockState anvil = level.getBlockState(info.getBlockPosition());
        return isAnvil(anvil) || anvil.getBlock() instanceof SmithingTableBlock;
    }

    @Override
    public boolean shouldRender(AnvilInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).isAir()
                && info.readyToRender();

    }

    @Override
    protected void render(AnvilInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeAnvil / info.getItemTransitionCountdown();

        // Render experience levels needed if we have an experience cost to show
        if (info.isReallyAnvil && info.anvilCost > 0) {
            renderText(new StringTextComponent("Levels Needed: " + info.anvilCost), stack,
                    info.textPos);
        }

        // Render the actual items
        for (int i = 0; i <= 2; i++) {
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, info.renderDirection, Direction.UP, info.getHitbox(i), false, -1);
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useAnvilImmersion;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo wInfo, ImmersiveStorage storageIn) {
        AnvilInfo info = (AnvilInfo) wInfo;
        info.items = storageIn.items;
        AnvilStorage aStorage = (AnvilStorage) storageIn;
        info.anvilCost = aStorage.xpLevels;
    }

    @Override
    public AnvilInfo getNewInfo(BlockPos pos) {
        return new AnvilInfo(pos, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderAnvil;
    }

    @Override
    protected boolean slotShouldRenderHelpHitbox(AnvilInfo info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    public void trackObject(BlockPos pos) {
        for (AnvilInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
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
