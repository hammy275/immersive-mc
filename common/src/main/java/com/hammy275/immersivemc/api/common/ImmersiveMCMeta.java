package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.common.api_impl.ImmersiveMCMetaImpl;

/**
 * Contains methods relating to ImmersiveMC itself, rather than an individual API feature of ImmersiveMC.
 * <br>
 * Note that the versioning methods here refer to the version of ImmersiveMC's API, rather than of the user-facing
 * version number, see {@link #compatibleWithAPIVersion(String)} for more info. ImmersiveMC's API version follows
 * "partial semantic versioning", where the version number is of the format "x.y", x being equivalent to MAJOR from
 * semantic versioning while y is to MINOR from semantic versioning. For example, ImmersiveMC 1.5.0 has the version
 * "2.0".
 */
public interface ImmersiveMCMeta {

    /**
     * @return An ImmersiveMCMeta instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    static ImmersiveMCMeta instance() {
        return ImmersiveMCMetaImpl.INSTANCE;
    }

    /**
     * Given an internal API version, determines if that API version is compatible with the currently-active version of
     * ImmersiveMC. Note that this is only for guaranteed compatibility, you may interact with ImmersiveMC in such
     * a way where several different, major ImmersiveMC versions are still compatible with your code. This should
     * be checked for manually by comparing against {@link #getMajorAPIVersion()} and {@link #getMinorAPIVersion()}.
     * @param builtVersion The internal API version to check with. This usually should be ImmersiveMC's internal API
     *                     version that your code was built with.
     * @return Whether your code is compatible under "partial semantic versioning" with this version of ImmersiveMC.
     * @throws IllegalArgumentException If an invalid version string is provided, such as one that doesn't follow
     *                                  semantic versioning.
     */
    boolean compatibleWithAPIVersion(String builtVersion) throws IllegalArgumentException;

    /**
     * Gets ImmersiveMC's internal API version as a string. If you want the user-facing ImmersiveMC version,
     * you should use the appropriate method from your modloader of choice.
     * @return ImmersiveMC's internal API version following "partial semantic versioning".
     */
    String getAPIVersion();

    /**
     * @return The major version number of ImmersiveMC's internal API. See {@link #getAPIVersion()} for more info.
     */
    int getMajorAPIVersion();

    /**
     * @return The minor version number of ImmersiveMC's internal API. See {@link #getAPIVersion()} for more info.
     */
    int getMinorAPIVersion();

}
