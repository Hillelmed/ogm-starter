package io.github.hillelmed.ogm.starter.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FolderFileUtil {

    public static void setWritable(Path rootDir) {
        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String os = System.getProperty("os.name");
                    if (os.toLowerCase().contains("windows")) {
                        setWritableWindowsFun(file);
                    } else {
                        setWritableUnixFun(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static void setWritableWindowsFun(Path file) throws IOException {
        DosFileAttributeView view = Files.getFileAttributeView(file, DosFileAttributeView.class);
        if (view != null) {
            DosFileAttributes attrs = view.readAttributes();
            if (!attrs.isReadOnly()) {
                view.setReadOnly(false);
            }
        }
    }

    private static void setWritableUnixFun(Path file) throws IOException {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(file, permissions);
    }


}
