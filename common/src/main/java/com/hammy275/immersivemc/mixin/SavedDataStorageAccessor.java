package com.hammy275.immersivemc.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Mixin(SavedDataStorage.class)
public interface SavedDataStorageAccessor {

    @Accessor("cache")
    public Map<SavedDataType<?>, Optional<SavedData>> immersiveMC$getCache();

    @Invoker("getDataFile")
    public Path immersiveMC$getDataFile(Identifier id);
}
