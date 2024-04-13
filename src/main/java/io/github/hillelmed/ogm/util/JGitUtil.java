package io.github.hillelmed.ogm.util;

import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.internal.storage.dfs.*;
import org.eclipse.jgit.transport.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class JGitUtil {

    public static InMemoryRepository getInMemoryRepository(UsernamePasswordCredentialsProvider credentialsProvider, String url) throws GitAPIException {
        DfsRepositoryDescription description = new DfsRepositoryDescription();
        InMemoryRepository inMemoryRepository = new InMemoryRepository(description);
        try (Git git = new Git(inMemoryRepository)) {
            git.fetch()
                    .setRemote(url)
                    .setCredentialsProvider(credentialsProvider)
                    .setRefSpecs(new RefSpec(GitConst.REFS_SPEC))
                    .call();
            inMemoryRepository.getObjectDatabase();
        }
        return inMemoryRepository;
    }
}
