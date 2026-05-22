package com.hammy275.immersivemc.server;

public class ServerMixinProxy {

    public static boolean pretendPlayerIsNotCrouching = false;

    /**
     * Needed since the decrement of openers to -1 is very bad, as when written as a count of number of openers then
     * read back, it's read as an unsigned byte (255). Good to protect from bad values here anyway though.
     */
    public static boolean skipIncrementDecrementChests = false;
}
