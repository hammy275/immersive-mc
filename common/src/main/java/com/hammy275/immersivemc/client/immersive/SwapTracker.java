package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.client.subscribe.ClientVRSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SwapTracker {

    public static final SwapTracker c0 = new SwapTracker(InteractionHand.MAIN_HAND);
    public static final SwapTracker c1 = new SwapTracker(InteractionHand.OFF_HAND);

    protected final InteractionHand hand;

    protected SwapState state = SwapState.NONE;
    protected int mostRecentHitbox = -1;
    protected int ticksInMostRecent = 0;
    protected LastImmersive<?> lastImmersive = null;
    protected final Set<Integer> queuedPlacements = new LinkedHashSet<>(); // LinkedHashSet to remember order
    protected boolean leftClickWasDown = false;
    protected boolean lastTickWasIdle = false;
    protected int rightClickCooldown = 0; // Needs to be kept here, since Minecraft sets the cooldown before interactions

    public SwapTracker(InteractionHand hand) {
        this.hand = hand;
    }

    /**
     * Workaround so non-VR play can do a NONE tick
     */
    public void maybeIdleTick() {
        if (lastTickWasIdle) {
            tick(null, null, -1, false);
        }
        lastTickWasIdle = true;
    }

    /**
     * Tick the swap tracker with data.
     *
     * @param immersive    Immersive being interacted with, or null if there is none.
     * @param info         Info being interacted with, or null if no info.
     * @param inputHitbox  Input hitbox being interacted with. Should be negative if not interacting with one.
     * @param inDragHitbox Whether we're currently in the drag hitbox. Should be false if dragging is disabled.
     */
    public <I extends ImmersiveInfo> void tick(@Nullable Immersive<I, ?> immersive, @Nullable I info, int inputHitbox, boolean inDragHitbox) {
        // Update state machine
        lastTickWasIdle = false;
        if (this.mostRecentHitbox < 0) {
            this.ticksInMostRecent = 0;
        } else {
            this.ticksInMostRecent++;
        }

        if (this.rightClickCooldown > 0) {
            this.rightClickCooldown--;
        }

        if (this.state == SwapState.NONE && inputHitbox >= 0) {
            if (Minecraft.getInstance().options.keyAttack.isDown()) {
                setState(SwapState.DRAG, inputHitbox);
            } else {
                setState(SwapState.PLACE, inputHitbox);
            }
        } else if (!inDragHitbox) {
            setState(SwapState.NONE, -1);
        } else if (this.state != SwapState.DRAG && Minecraft.getInstance().options.keyAttack.isDown()) {
            setState(SwapState.DRAG, inputHitbox);
        } else if (this.state == SwapState.PLACE && inDragHitbox && inputHitbox != this.mostRecentHitbox) {
            setState(SwapState.DRAG, inputHitbox);
        } else if (this.state == SwapState.DRAG && this.ticksInMostRecent >= 8 && this.mostRecentHitbox == inputHitbox) {
            setState(SwapState.PLACE, inputHitbox);
        } else if (this.state == SwapState.DRAG && !Minecraft.getInstance().options.keyAttack.isDown() && this.leftClickWasDown) {
            setState(SwapState.PLACE, inputHitbox);
        }

        // Update lastImmersive and do interactions
        if (immersive != null && info != null) {
            LastImmersive<?> newLI = new LastImmersive<>(immersive, info);
            if (!newLI.equals(this.lastImmersive)) {
                maybeDoDragPlace();
            }
            this.lastImmersive = newLI;
        }
        if (this.state == SwapState.PLACE) {
            this.rightClickCooldown = this.lastImmersive.doHitboxInteract(List.of(inputHitbox), this.hand, this.rightClickCooldown, Minecraft.getInstance().options.keyAttack.isDown());
        } else if (this.state == SwapState.DRAG && inputHitbox >= 0) {
            queuedPlacements.add(inputHitbox);
        }
    }

    protected void setState(SwapState newState, int newHitbox) {
        if (newHitbox != this.mostRecentHitbox) {
            this.ticksInMostRecent = 0;
        }
        if (newState != SwapState.DRAG) {
            maybeDoDragPlace();
            this.leftClickWasDown = false;
        } else {
            this.leftClickWasDown = this.leftClickWasDown || Minecraft.getInstance().options.keyAttack.isDown();
        }
        if (newState == SwapState.NONE) {
            this.lastImmersive = null;
        }
        this.state = newState;
        this.mostRecentHitbox = newHitbox;
    }

    protected void maybeDoDragPlace() {
        if (this.state == SwapState.DRAG && !this.queuedPlacements.isEmpty()) {
            // Drag place passes 0 for the cooldown so placement always happens
            this.rightClickCooldown = this.lastImmersive.doHitboxInteract(this.queuedPlacements.stream().toList(), this.hand, 0, this.leftClickWasDown);
            this.queuedPlacements.clear();
        }
    }

    protected enum SwapState {
        NONE,
        PLACE,
        DRAG
    }

    protected record LastImmersive<I extends ImmersiveInfo>(Immersive<I, ?> immersive, I info) {

        public int doHitboxInteract(List<Integer> slots, InteractionHand hand, int rightClickCooldown, boolean leftClickDown) {
            int currentCooldown = VRPluginVerify.clientInVR() && !ActiveConfig.active().rightClickImmersiveInteractionsInVR
                    ? ClientVRSubscriber.getCooldown() : rightClickCooldown;
            if (currentCooldown <= 0) {
                int cooldown = immersive.handleHitboxInteract(info, Minecraft.getInstance().player, slots, hand, leftClickDown);
                ImmersiveClientLogicHelpers.instance().setCooldown(cooldown);
                return cooldown;
            }
            return currentCooldown;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LastImmersive<?> other) {
                return this.immersive == other.immersive && this.info == other.info;
            }
            return false;
        }
    }
}
