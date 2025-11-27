package com.hammy275.immersivemc.server.immersive.petting;

import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.mixin.WolfInvoker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class VanillaMobsPettingHandler<P extends Animal> implements PettingHandler<P> {
    @Override
    public boolean matchesEntity(ServerPlayer playerPetting, Entity possibleEntityToPet) {
        return (possibleEntityToPet instanceof TamableAnimal ta && ta.isTame()) ||
                (possibleEntityToPet instanceof AbstractHorse horse && horse.isTamed());
    }

    @Override
    public int doPetEffect(ServerPlayer playerPetting, P petEntity, Vec3 handPosition) {
        if (ThreadLocalRandom.current().nextInt(5) == 0) {
            SoundEvent sound = null;
            if (petEntity instanceof Wolf wolf) {
                sound = ((WolfInvoker) wolf).immersiveMC$getSoundVariant().value().pantSound().value();
            } else if (petEntity instanceof Cat) {
                sound = SoundEvents.CAT_PURREOW;
            } else if (petEntity instanceof Horse) {
                sound = SoundEvents.HORSE_AMBIENT;
            } else if (petEntity instanceof Donkey) {
                sound = SoundEvents.DONKEY_AMBIENT;
            } else if (petEntity instanceof Mule) {
                sound = SoundEvents.MULE_AMBIENT;
            }
            if (sound != null) {
                playerPetting.serverLevel().playSound(null, petEntity, sound, SoundSource.NEUTRAL, 1f, 1f);
            }
            return 10;
        }
        return 0;
    }
}
