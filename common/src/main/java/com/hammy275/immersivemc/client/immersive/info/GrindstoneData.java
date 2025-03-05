package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;

public class GrindstoneData {

    private static final int TICKS_TO_GRIND = 10;

    public final GrindHand[] grindHands = new GrindHand[]{new GrindHand(), new GrindHand()};

    public GrindstoneData() {}

    public boolean didGrind(InteractionHand hand) {
        return grindHands[hand.ordinal()].didGrind();
    }

    public void resetGrind(InteractionHand hand) {
        grindHands[hand.ordinal()].resetGrind();
    }

    public boolean grindTick(InteractionHand hand) {
        Player player = Minecraft.getInstance().player;
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            GrindstoneMenu menu = new GrindstoneMenu(-1, player.getInventory());
            if (menu.slots.get(0).mayPlace(stack)) {
                menu.slots.get(0).set(stack);
                if (!menu.slots.get(2).getItem().isEmpty()) {
                    grindHands[hand.ordinal()].grindTick();
                    return true;
                }
            }
        }
        return false;
    }

    public static class GrindHand {
        public int startTick;
        public int lastTick;

        private GrindHand() {
            resetGrind();
        }

        public boolean didGrind() {
            return lastTick - startTick >= TICKS_TO_GRIND && startTick >= 0;
        }

        public void resetGrind() {
            this.startTick = -1;
            this.lastTick = -1;
        }

        public void grindTick() {
            int currentTick = Minecraft.getInstance().player.tickCount;
            if (currentTick - 1 == lastTick || currentTick - 2 == lastTick) {
                lastTick++;
            } else {
                startTick = currentTick;
                lastTick = currentTick;
            }
        }
    }
}
