package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SelfHandlingNetworkStorageSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage.CHEST_OPEN_THRESHOLD;

public class ChestInfo extends AbstractImmersiveInfo {

    public BlockEntity chest;
    public BlockEntity otherChest;
    public BlockPos otherPos = null;
    public Direction forward = null;
    protected int rowNum = 0;
    public List<BoundingBox> openCloseHitboxes = new ArrayList<>();
    public Vec3 openClosePosition = null;
    public int light = ClientUtil.maxLight;
    private @Nullable ChestOpennessStorage opennessStorage = null;

    public ChestInfo(BlockEntity chest, BlockEntity otherChest) {
        super(chest.getBlockPos()); // Accounts for double chest
        this.chest = chest;
        this.otherChest = otherChest;
        if (this.otherChest != null) {
            this.otherPos = this.otherChest.getBlockPos();
        } else {
            this.otherPos = null;
        }
        for (int i = 0; i < 54; i++) {
            hitboxes.add(new HitboxItemPair(null, ItemStack.EMPTY, false));
        }
    }

    public void nextRow() {
        rowNum = getNextRow(rowNum);
    }

    public int getNextRow(int rowIn) {
        if (++rowIn > 2) {
            return 0;
        }
        return rowIn;
    }

    public int getRowNum() {
        return rowNum;
    }

    @Override
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return (hitboxes.get(8).box != null || hitboxes.get(17).box != null || hitboxes.get(26).box != null) &&
                (this.otherChest == null || (hitboxes.get(35).box != null || hitboxes.get(44).box != null || hitboxes.get(53).box != null));
    }

    public boolean isOpen() {
        return getDirectOpenness() >= CHEST_OPEN_THRESHOLD;
    }

    public void setOpennessStorage(ChestOpennessStorage opennessStorage) {
        this.opennessStorage = opennessStorage;
    }

    public float getForcedOpenness() {
        return opennessStorage == null ? -1 : opennessStorage.getOpenness();
    }

    public boolean takeControl(ChestOpennessStorage.AnimationState animationState) {
        return opennessStorage != null && opennessStorage.takeControl(Minecraft.getInstance().player.getUUID(), animationState);
    }

    public void syncOpennessToServerIfDirty() {
        if (!Minecraft.getInstance().player.getUUID().equals(opennessStorage.getControllingPlayerUUID())) {
            throw new RuntimeException("Illegal state: Player should own chest before syncing to server.");
        } else if (opennessStorage.isDirty()) {
            Network.INSTANCE.sendToServer(new SelfHandlingNetworkStorageSyncPacket(opennessStorage));
        }
    }

    public void setForcedOpenness(float forcedOpenness) {
        opennessStorage.setOpenness(forcedOpenness);
    }

    /**
     * Gets the openness from the chest, ignoring the forced openness. Mainly useful since the non-VR code path for
     * opening/closing chests doesn't modify forcedOpenness.
     * @return The openness of the chest as rendered in-world.
     */
    public float getDirectOpenness() {
        if (chest instanceof LidBlockEntity lbe) {
            return lbe.getOpenNess(1f);
        }
        return -1f;
    }
}
