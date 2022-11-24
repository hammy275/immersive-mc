package com.hammy275.immersivemc.common.immersive;

@FunctionalInterface
public interface CheckerFunction<A, B, C, D, RET> {
    RET apply(A a, B b, C c, D d);
}
