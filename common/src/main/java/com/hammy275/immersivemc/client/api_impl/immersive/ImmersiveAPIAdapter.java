package com.hammy275.immersivemc.client.api_impl.immersive;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A temporary adapter to allow the API to work with ImmersiveMC during API development.
 */
public class ImmersiveAPIAdapter<I extends ImmersiveInfo, S extends NetworkStorage> extends AbstractImmersive<ImmersiveInfoAPIAdapter<I>, S> {

    private final Immersive<I, S> apiImmersive;

    public ImmersiveAPIAdapter(Immersive<I, S> apiImmersive) {
        super(-1);
        this.apiImmersive = apiImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return apiImmersive.clientAuthoritative();
    }

    @Override
    public @Nullable ImmersiveHandler<S> getHandler() {
        return apiImmersive.getHandler();
    }

    @Override
    public boolean shouldRender(ImmersiveInfoAPIAdapter<I> info, boolean isInVR) {
        return apiImmersive.shouldRender(info.apiInfo);
    }

    @Override
    protected void render(ImmersiveInfoAPIAdapter<I> info, PoseStack stack, boolean isInVR) {
        apiImmersive.render(info.apiInfo, stack, ImmersiveRenderHelpersImpl.INSTANCE, Minecraft.getInstance().getFrameTime());
    }

    @Override
    protected void doTick(ImmersiveInfoAPIAdapter<I> info, boolean isInVR) {
        super.doTick(info, isInVR);
        apiImmersive.tick(info.apiInfo);
    }

    @Override
    public boolean enabledInConfig() {
        return apiImmersive.getHandler().enabledInConfig(Minecraft.getInstance().player);
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(ImmersiveInfoAPIAdapter<I> info, int slotNum) {
        return false;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return apiImmersive.getHandler().isValidBlock(pos, level);
    }

    @Override
    public @Nullable ImmersiveInfoAPIAdapter<I> refreshOrTrackObject(BlockPos pos, Level level) {
        for (ImmersiveInfoAPIAdapter<I>  info : infos) {
            if (info.getBlockPosition().equals(pos)) {
                return info;
            }
        }
        I info = apiImmersive.buildInfo(pos, level);
        ImmersiveInfoAPIAdapter<I> adaptedInfo = new ImmersiveInfoAPIAdapter<>(info);
        infos.add(adaptedInfo);
        return adaptedInfo;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return apiImmersive.shouldDisableRightClicksWhenInteractionsDisabled(getAPIInfo(info));
    }

    @Override
    protected void initInfo(ImmersiveInfoAPIAdapter<I> info) {
        // Intentional NO-OP.
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        int runningSlot = 0;
        I apiInfo = getAPIInfo(info);
        for (int i = 0; i < apiInfo.getAllHitboxes().size(); i++) {
            HitboxInfo hbox = apiInfo.getAllHitboxes().get(i);
            if (!hbox.isTriggerHitbox()) {
                if (runningSlot == closest) {
                    apiImmersive.handleHitboxInteract(apiInfo, Minecraft.getInstance().player, i, hand);
                    break;
                } else {
                    runningSlot++;
                }
            }
        }
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        int runningSlot = 0;
        I apiInfo = getAPIInfo((AbstractImmersiveInfo) info);
        for (int i = 0; i < apiInfo.getAllHitboxes().size(); i++) {
            HitboxInfo hbox = apiInfo.getAllHitboxes().get(i);
            if (hbox.isTriggerHitbox()) {
                if (runningSlot == hitboxNum) {
                    apiImmersive.handleHitboxInteract(apiInfo, Minecraft.getInstance().player, i, InteractionHand.MAIN_HAND);
                    break;
                } else {
                    runningSlot++;
                }
            }
        }
    }

    @Override
    public void processStorageFromNetwork(AbstractImmersiveInfo info, S storage) {
        apiImmersive.processStorageFromNetwork(getAPIInfo(info), storage);
    }

    @Override
    public BlockPos getLightPos(ImmersiveInfoAPIAdapter<I> info) {
        return info.getBlockPosition(); // Does a useless light calculation, but need to provide a valid value here.
    }

    @Override
    public void globalTick() {
        // Reconstruct the API implementation with our state first
        Collection<I> rawInfos = apiImmersive.getTrackedObjects();
        rawInfos.clear();
        for (ImmersiveInfoAPIAdapter<I> adaptedInfo : infos) {
            rawInfos.add(adaptedInfo.apiInfo);
            adaptedInfo.apiInfo.setSlotHovered(adaptedInfo.slotHovered, 0);
            adaptedInfo.apiInfo.setSlotHovered(adaptedInfo.slotHovered2, 1);
        }
        // Pass off to API's global tick
        apiImmersive.globalTick();
    }

    @SuppressWarnings("unchecked")
    private I getAPIInfo(AbstractImmersiveInfo infoIn) {
        ImmersiveInfoAPIAdapter<I> adaptedInfo = (ImmersiveInfoAPIAdapter<I>) infoIn;
        return adaptedInfo.apiInfo;
    }
}
