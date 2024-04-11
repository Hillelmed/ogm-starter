package io.github.hillelmed.ogm.config;

import lombok.*;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Data
public class OgmConfig {

    private UsernamePasswordCredentialsProvider credentials;
    private String url;

}
