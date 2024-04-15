package io.github.hillelmed.ogm.util;

import lombok.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            e.printStackTrace();
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
