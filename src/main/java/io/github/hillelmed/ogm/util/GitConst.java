package io.github.hillelmed.ogm.util;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitConst {

    public static final String GIT_REF = "refs/heads/";
    public static final String REFS_SPEC = "+refs/*:refs/*";
}
