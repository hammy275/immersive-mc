package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.util.CommonMixinProxy;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;

@Mixin(SavedDataStorage.class)
public class SavedDataStorageMixin {

    @Shadow
    @Final
    private Path dataFolder;

    @WrapMethod(method = "getDataFile")
    private Path immersiveMC$getOldDataFiles(Identifier id, Operation<Path> original) {
        return CommonMixinProxy.getSavedDataOldPath(id, dataFolder, original);
    }
}
