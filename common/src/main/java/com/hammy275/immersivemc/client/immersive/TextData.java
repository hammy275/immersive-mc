package com.hammy275.immersivemc.client.immersive;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record TextData(Component text, Vec3 offset) {
}
