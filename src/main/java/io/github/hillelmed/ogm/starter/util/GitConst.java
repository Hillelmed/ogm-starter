package io.github.hillelmed.ogm.starter.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The type Git const.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitConst {

    /**
     * The constant GIT_REF.
     */
    public static final String GIT_REF = "refs/heads/";
    /**
     * The constant REFS_SPEC.
     */
    public static final String REFS_SPEC = "+refs/*:refs/*";
}
