package com.hammy275.immersivemc.test;

/**
 * Assertion failure exception.
 * <br>
 * This exception is specially handled in {@link TestResult} to get the third line of the traceback and print
 * that, rather than the entire traceback. As such, this should only be thrown from a method that is exactly
 * one method call away from the test.
 */
public class AssertionFailure extends RuntimeException {
    public AssertionFailure(String msg) {
        super(msg);
    }
}
