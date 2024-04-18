package io.github.hillelmed.ogm.starter.config;

import lombok.*;
import org.eclipse.jgit.transport.*;

@Data
public class OgmConfig {

    private UsernamePasswordCredentialsProvider credentials;
    private String url;

}
