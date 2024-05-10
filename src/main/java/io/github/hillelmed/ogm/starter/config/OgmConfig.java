package io.github.hillelmed.ogm.starter.config;

import lombok.Data;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Data
public class OgmConfig {

    private UsernamePasswordCredentialsProvider credentials;
    private String url;

}
