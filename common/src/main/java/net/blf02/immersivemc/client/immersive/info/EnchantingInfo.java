package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantingInfo extends AbstractWorldStorageInfo {

    public boolean areaAboveIsAir = false;
    public final int[] yOffsetPositions = new int[]{0,
            ThreadLocalRandom.current().nextInt(10),
            ThreadLocalRandom.current().nextInt(15),
            ThreadLocalRandom.current().nextInt(20)};

    public int lookingAtIndex = -1;
    public Direction lastDir = null;
    public ItemStack itemEnchantedCopy = ItemStack.EMPTY;

    public final ETableInfo weakInfo = new ETableInfo();
    public final ETableInfo midInfo = new ETableInfo();
    public final ETableInfo strongInfo = new ETableInfo();

    public EnchantingInfo(BlockPos pos, int ticksToExist) {
        // Note that items[1], items[2], and items[3] go unused. Instead, all reference this.itemEnchantedCopy
        // We specify the existance of up to index 3 so we can use up to positions[3] and hitboxes[3]
        super(pos, ticksToExist, 3);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 1);
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes()
                 && areaAboveIsAir;
    }

    public static class ETableInfo {
        public int levelsNeeded;
        public Component textPreview = null;

        public boolean isPresent() {
            return this.textPreview != null;
        }
    }
}
