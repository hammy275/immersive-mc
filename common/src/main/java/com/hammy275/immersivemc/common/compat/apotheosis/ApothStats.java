package com.hammy275.immersivemc.common.compat.apotheosis;

import net.minecraft.network.FriendlyByteBuf;

public record ApothStats(float eterna, float quanta, float arcana, int clues) {

    public static ApothStats EMPTY = new ApothStats(0, 0, 0, 1);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(eterna).writeFloat(quanta).writeFloat(arcana).writeInt(clues);
    }

    public static ApothStats decode(FriendlyByteBuf buffer) {
        return new ApothStats(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }
}
