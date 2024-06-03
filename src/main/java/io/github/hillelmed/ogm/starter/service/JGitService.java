package io.github.hillelmed.ogm.starter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.hillelmed.ogm.starter.annotation.GitFile;
import io.github.hillelmed.ogm.starter.config.OgmConfig;
import io.github.hillelmed.ogm.starter.domain.FileType;
import io.github.hillelmed.ogm.starter.domain.GitRepositoryMap;
import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.github.hillelmed.ogm.starter.util.FolderFileUtil;
import io.github.hillelmed.ogm.starter.util.GitConst;
import io.github.hillelmed.ogm.starter.util.OgmAppUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.hillelmed.ogm.starter.util.OgmAppUtil.readByType;

/**
 * The type J git service.
 */
@Slf4j
@RequiredArgsConstructor
public class JGitService {

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    private final YAMLMapper yamlMapper;

    /**
     * Gets git in memory.
     *
     * @param credentialsProvider the credentials provider
     * @param url                 the url
     * @return the git in memory
     * @throws GitAPIException the git api exception
     */
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

    /**
     * Load remote git repository map.
     *
     * @param git      the git
     * @param revision the revision
     * @param include  the include
     * @return the git repository map
     * @throws IOException the io exception
     */
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

    /**
     * Load remote spesific file object.
     *
     * @param gitInMemoryRepository the git in memory repository
     * @param revision              the revision
     * @param fileType              the file type
     * @param pathFile              the path file
     * @return the object
     * @throws IOException the io exception
     */
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


    /**
     * Write file and push.
     *
     * @param ogmConfig            the ogm config
     * @param repositoryFieldValue the repository field value
     * @param branchFieldValue     the branch field value
     * @param content              the content
     * @param gitFileAnnotation    the git file annotation
     * @param isUpdateExistFile    the is update exist file
     * @throws IOException the io exception
     */
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
                throw new UnsupportedEncodingException("File does not exist use create method for create this file");
            }
            OgmAppUtil.writeFileByType(xmlMapper, jsonMapper, yamlMapper, gitFileAnnotation.type(), content, filePath);
            commitMessageAndPush(git, ogmConfig);
        } catch (GitAPIException e) {
            throw new OgmRuntimeException(e);
        }

    }

    /**
     * Sync files and push.
     *
     * @param ogmConfig            the ogm config
     * @param repositoryFieldValue the repository field value
     * @param branchFieldValue     the branch field value
     * @param contentFiles         the content files
     * @param isUpdateExistFile    the is update exist file
     * @throws IOException the io exception
     */
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
            contentFiles.getKeyPathAddedOrChange().forEach((String path) -> {
                String filePath = git.getRepository().getDirectory()
                        .getAbsolutePath().replace(".git", "") + path;
                if (isUpdateExistFile && !new File(filePath).exists()) {
                    try {
                        throw new UnsupportedEncodingException("File does not exist use create method for create this file");
                    } catch (UnsupportedEncodingException e) {
                        throw new OgmRuntimeException(e);
                    }
                }
                try {
                    OgmAppUtil.findTypeAndWriteFile(xmlMapper, jsonMapper, yamlMapper, contentFiles.get(path), filePath);
                } catch (IOException e) {
                    throw new OgmRuntimeException(e);
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
                    throw new OgmRuntimeException(e);
                }
            });
            commitMessageAndPush(git, ogmConfig);
        } catch (GitAPIException e) {
            throw new OgmRuntimeException(e);
        }

    }

    /**
     * Commit message and push.
     *
     * @param git       the git
     * @param ogmConfig the ogm config
     * @throws GitAPIException the git api exception
     */
    void commitMessageAndPush(Git git, OgmConfig ogmConfig) throws GitAPIException {
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

    /**
     * Call add and dont have diff string.
     *
     * @param git the git
     * @return the string
     * @throws GitAPIException the git api exception
     */
    String callAddAndDontHaveDiff(Git git) throws GitAPIException {
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
