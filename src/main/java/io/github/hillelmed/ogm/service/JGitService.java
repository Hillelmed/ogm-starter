package io.github.hillelmed.ogm.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.github.hillelmed.ogm.annotation.*;
import io.github.hillelmed.ogm.config.*;
import io.github.hillelmed.ogm.domain.*;
import io.github.hillelmed.ogm.util.*;
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
import java.util.concurrent.atomic.*;

import static io.github.hillelmed.ogm.util.OgmAppUtil.*;

@Slf4j
@RequiredArgsConstructor
public class JGitService {

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
                return git;
            } catch (GitAPIException e) {
                log.error(e.getMessage());
                throw e;
            }
        }
    }

    public GitRepositoryMap loadRemote(Git git, String revision, String[] include) throws IOException {
        TreeWalk treeWalk = loadGit(git, revision);
        InMemoryRepository repo = (InMemoryRepository) git.getRepository();
        if (include != null && include.length > 0) {
            Collection<PathFilter> pathFilters = Arrays.stream(include).map(PathFilter::create).toList();
            treeWalk.setFilter(PathFilterGroup.create(pathFilters));
        }
        GitRepositoryMap files = new GitRepositoryMap();
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            loader.copyTo(byteArrayOutputStream);
            files.put(path, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        }
        files.removeAllKeyPathAddedOrChange();
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
                                 GitFile gitFileAnnotation, boolean isUpdateExistFile) throws IOException {
        File tmpdir = Files.createTempDirectory("OgmJGit").toFile();
        FolderFileUtil.setWritable(tmpdir.toPath());
        try (Git git = Git.cloneRepository()
                .setBare(false)
                .setCredentialsProvider(ogmConfig.getCredentials())
                .setBranch(GitConst.GIT_REF + branchFieldValue)
                .setURI(ogmConfig.getUrl() + "/" + repositoryFieldValue)
                .setDirectory(tmpdir)
                .call()) {
            String filePath = git.getRepository().getDirectory()
                    .getAbsolutePath().replace(".git", "") + gitFileAnnotation.path();
            if (isUpdateExistFile && !new File(filePath).exists()) {
                throw new UnsupportedEncodingException("File does not exist use create for create the file");
            }
            OgmAppUtil.writeFileByType(xmlMapper, jsonMapper, yamlMapper, gitFileAnnotation.type(), content, filePath);
            commitMessageAndPush(git, ogmConfig);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }

    public void syncFilesAndPush(OgmConfig ogmConfig, String repositoryFieldValue,
                                 String branchFieldValue,
                                 GitRepositoryMap contentFiles, boolean isUpdateExistFile) throws IOException {
        File tmpdir = Files.createTempDirectory("OgmJGit").toFile();
        FolderFileUtil.setWritable(tmpdir.toPath());
        try (Git git = Git.cloneRepository()
                .setBare(false)
                .setCredentialsProvider(ogmConfig.getCredentials())
                .setBranch(GitConst.GIT_REF + branchFieldValue)
                .setURI(ogmConfig.getUrl() + "/" + repositoryFieldValue)
                .setDirectory(tmpdir)
                .call()) {
            contentFiles.getKeyPathAddedOrChange().forEach((path) -> {
                String filePath = git.getRepository().getDirectory()
                        .getAbsolutePath().replace(".git", "") + path;
                if (isUpdateExistFile && !new File(filePath).exists()) {
                    try {
                        throw new UnsupportedEncodingException("File does not exist use create for create the file");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    OgmAppUtil.findTypeAndWriteFile(xmlMapper, jsonMapper, yamlMapper, contentFiles.get(path), filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            AtomicBoolean deletedFiles = new AtomicBoolean(false);
            contentFiles.getKeyPathDeleted().forEach(path -> {
                String filePath = git.getRepository().getDirectory()
                        .getAbsolutePath().replace(".git", "") + path;
                try {
                    Files.delete(Path.of(filePath));
                    deletedFiles.compareAndSet(false, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            commitMessageAndPush(git, ogmConfig);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }

    private void commitMessageAndPush(Git git, OgmConfig ogmConfig) throws GitAPIException {
        String commitMessage = callAddAndDontHaveDiff(git);
        if (commitMessage == null) {
            return;
        }
        git.commit().setMessage(commitMessage).call();
        // push to remote:
        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(ogmConfig.getCredentials());
        // you can add more settings here if needed
        pushCommand.call();
    }

    private String callAddAndDontHaveDiff(Git git) throws GitAPIException {
        git.add().setUpdate(true).addFilepattern(".").call();
        git.add().addFilepattern(".").call();
        // Get the list of differences (changes)
        Status status = git.status().call();
        Set<String> uncommittedChanges = status.getUncommittedChanges();
        if (uncommittedChanges.isEmpty()) {
            log.debug("No changes detected");
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        status.getAdded().forEach(s -> stringBuilder.append("File added: ").append(s).append("\n"));
        status.getChanged().forEach(s -> stringBuilder.append("File changed: ").append(s).append("\n"));
        status.getRemoved().forEach(s -> stringBuilder.append("File removed: ").append(s).append("\n"));
        status.getMissing().forEach(s -> stringBuilder.append("File missing: ").append(s).append("\n"));
        status.getModified().forEach(s -> stringBuilder.append("File modified: ").append(s).append("\n"));
        status.getConflicting().forEach(s -> stringBuilder.append("File conflicting: ").append(s).append("\n"));
        log.debug(stringBuilder.toString());
        return stringBuilder.toString();
    }
}
