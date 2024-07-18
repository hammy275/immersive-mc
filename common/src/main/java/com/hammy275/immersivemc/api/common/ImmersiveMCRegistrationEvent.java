package com.hammy275.immersivemc.api.common;

/**
 * An event that can be fired at any time, but is only fired once for each object type. Used to register objects
 * related to ImmersiveMC with ImmersiveMC. This can fire BEFORE registries are available on Forge and NeoForge!
 * @param <T> The type of object to register.
 */
public interface ImmersiveMCRegistrationEvent<T> {

    /**
     * Register one or more objects with ImmersiveMC.
     * @param objects The object or objects to register.
     */
    public void register(T... objects);


    /**
     * Register multiple objects with ImmersiveMC.
     * @param objects The objects to register.
     */
    public void register(Iterable<T> objects);
}
