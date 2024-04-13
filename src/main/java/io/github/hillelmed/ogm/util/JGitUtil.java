package io.github.hillelmed.ogm.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import io.github.hillelmed.ogm.domain.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.internal.storage.dfs.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static io.github.hillelmed.ogm.util.OgmAppUtil.*;

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

    public static Map<String, String> loadRemote(InMemoryRepository repo, String url, String revision, String[] include) throws Exception {
        Git git = new Git(repo);
        git.fetch().setRemote(url).setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*")).call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + revision);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        if (include != null && include.length > 0) {
            Collection<PathFilter> pathFilters = Arrays.stream(include).map(PathFilter::create).toList();
            treeWalk.setFilter(PathFilterGroup.create(pathFilters));
        }
        Map<String, String> files = new HashMap<>();
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            loader.copyTo(byteArrayOutputStream);
            files.put(path, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        }
        return files;
    }

    public static Object loadRemoteSpesificFile(
            XmlMapper xmlMapper,
            ObjectMapper jsonMapper,
            ObjectMapper yamlMapper,
            InMemoryRepository repo, String url, String revision, FileType fileType, String pathFile) throws Exception {
        Git git = new Git(repo);
        git.fetch().setRemote(url).setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*")).call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + revision);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(pathFile));
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            if (path.equals(pathFile)) {
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repo.open(objectId);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                loader.copyTo(byteArrayOutputStream);
                return readByType(xmlMapper, jsonMapper, yamlMapper, fileType, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
            }
        }
        return null;
    }


}
