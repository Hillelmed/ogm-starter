package io.github.hillelmed.ogm.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.config.*;
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
import java.nio.file.*;
import java.util.*;

import static io.github.hillelmed.ogm.util.OgmAppUtil.*;

@Slf4j
@RequiredArgsConstructor
public class JGitUtil {

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    private final YAMLMapper yamlMapper;

    public Git getGitInMemory(UsernamePasswordCredentialsProvider credentialsProvider, String url) throws GitAPIException {
        DfsRepositoryDescription description = new DfsRepositoryDescription();
        InMemoryRepository inMemoryRepository = new InMemoryRepository(description);
        try (Git git = new Git(inMemoryRepository)) {
            try {
                git.fetch()
                        .setRemote(url)
                        .setCredentialsProvider(credentialsProvider)
                        .setRefSpecs(new RefSpec(GitConst.REFS_SPEC))
                        .call();
                inMemoryRepository.getObjectDatabase();
                return git;
            } catch (GitAPIException e) {
                log.error(e.getMessage());
                throw e;
            }
        }
    }

    public Map<String, String> loadRemote(Git git, String revision, String[] include) throws IOException {
        TreeWalk treeWalk = loadGit(git, revision);
        InMemoryRepository repo = (InMemoryRepository) git.getRepository();
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

    private TreeWalk loadGit(Git git, String revision) throws IOException {
        InMemoryRepository repo = (InMemoryRepository) git.getRepository();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + revision);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        return treeWalk;
    }

    public Object loadRemoteSpesificFile(Git gitInMemoryRepository, String revision, FileType fileType, String pathFile) throws IOException {
        TreeWalk treeWalk = loadGit(gitInMemoryRepository, revision);
        InMemoryRepository repo = (InMemoryRepository) gitInMemoryRepository.getRepository();
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


    public void writeFileAndPush(OgmConfig ogmConfig, String repositoryFieldValue,
                                 String branchFieldValue,
                                 Object content,
                                 GitFile gitFileAnnotation) throws IOException {
        File tmpdir = Files.createTempDirectory("OgmJGit").toFile();
        FolderFileUtil.setWritable(tmpdir.toPath());
        try (Git git = Git.cloneRepository()
                .setBare(false)
                .setCredentialsProvider(ogmConfig.getCredentials())
                .setBranch(GitConst.GIT_REF + branchFieldValue)
                .setURI(ogmConfig.getUrl() + "/" + repositoryFieldValue)
                .setDirectory(tmpdir)
                .call()) {
            OgmAppUtil.writeFileByType(xmlMapper, jsonMapper, yamlMapper, gitFileAnnotation.type(), content, git.getRepository().getDirectory()
                    .getAbsolutePath().replace(".git", "") + gitFileAnnotation.path());
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Change file:" + gitFileAnnotation.path()).call();
            // push to remote:
            PushCommand pushCommand = git.push();
            pushCommand.setCredentialsProvider(ogmConfig.getCredentials());
            // you can add more settings here if needed
            pushCommand.call();
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }

    }
}
