package com.hammy275.immersivemc.client.api_impl.immersive;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
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
        apiImmersive.render(info.apiInfo, ImmersiveRenderHelpersImpl.INSTANCE);
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
        return apiImmersive.slotShouldRenderItemGuide(info.apiInfo, slotNum);
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return apiImmersive.getHandler().isValidBlock(pos, level);
    }

    @Override
    public @Nullable ImmersiveInfoAPIAdapter<I> refreshOrTrackObject(BlockPos pos, Level level) {
        Collection<I> infos = apiImmersive.getTrackedObjects();
        for (I info : infos) {
            if (info.getBlockPosition().equals(pos)) {
                return new ImmersiveInfoAPIAdapter<>(info);
            }
        }
        I info = apiImmersive.buildInfo(pos);
        infos.add(info);
        return new ImmersiveInfoAPIAdapter<>(info);
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
        apiImmersive.handleHitboxInteract(getAPIInfo(info), Minecraft.getInstance().player, closest, hand);
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
        apiImmersive.globalTick();
    }

    @SuppressWarnings("unchecked")
    private I getAPIInfo(AbstractImmersiveInfo infoIn) {
        ImmersiveInfoAPIAdapter<I> adaptedInfo = (ImmersiveInfoAPIAdapter<I>) infoIn;
        return adaptedInfo.apiInfo;
    }
}
