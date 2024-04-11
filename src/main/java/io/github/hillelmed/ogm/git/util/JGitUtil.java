package io.github.hillelmed.ogm.git.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class JGitUtil {

    public static InMemoryRepository getInMemoryRepository(UsernamePasswordCredentialsProvider credentialsProvider, String url) throws GitAPIException {
        DfsRepositoryDescription description = new DfsRepositoryDescription();
        InMemoryRepository inMemoryRepository = new InMemoryRepository(description);
        try(Git git = new Git(inMemoryRepository)) {
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
