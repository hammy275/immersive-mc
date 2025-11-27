package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;

import java.util.function.Consumer;

/**
 * Contains methods for registering Immersives with ImmersiveMC.
 */
public interface ImmersiveMCRegistration {

    /**
     * @return An ImmersiveMCRegistration instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveMCRegistration instance() {
        return ImmersiveMCRegistrationImpl.INSTANCE;
    }

    /**
     * Registers an object which, at some point, ImmersiveMC will call to register your {@link ImmersiveHandler}s.
     * The time at which registration occurs is only guaranteed to be some time after mods are initially constructed, so
     * handlers should be added here as early as possible, and be prepared for a lack of registry availability.
     * @param registrationHandler Your object that will register ImmersiveHandlers when called.
     * @throws IllegalStateException This method was called after registration.
     */
    public void addImmersiveHandlerRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>> registrationHandler) throws IllegalStateException;

    /**
     * Registers an object which, at some point, ImmersiveMC will call to register your {@link PettingHandler}s.
     * The time at which registration occurs is only guaranteed to be some time after mods are initially constructed, so
     * handlers should be added here as early as possible, and be prepared for a lack of registry availability.
     * @param registrationHandler Your object that will register PettingHandlers when called.
     * @throws IllegalStateException This method was called after registration.
     */
    public void addPettingHandlerRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<PettingHandler<?>>> registrationHandler) throws IllegalStateException;
}
