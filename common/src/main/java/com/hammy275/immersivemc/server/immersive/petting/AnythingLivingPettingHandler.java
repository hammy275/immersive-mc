package com.hammy275.immersivemc.server.immersive.petting;

import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AnythingLivingPettingHandler<P extends LivingEntity> implements PettingHandler<P> {
    @Override
    public boolean matchesEntity(ServerPlayer playerPetting, Entity possibleEntityToPet) {
        return ActiveConfig.getConfigForPlayer(playerPetting).allowPettingAnythingLiving && possibleEntityToPet instanceof LivingEntity;
    }

    @Override
    public int doPetEffect(ServerPlayer playerPetting, P petEntity, Vec3 handPosition) {
        // Intentional no-op, since we just display the built-in hearts.
        return 0;
    }
}
