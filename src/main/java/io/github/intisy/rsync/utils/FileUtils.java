package io.github.intisy.rsync.utils;

import io.github.intisy.rsync.Application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class FileUtils {
    public static Path copyResourceFolderToTemp(String resourceFolderPath) throws IOException, URISyntaxException {
        if (!resourceFolderPath.startsWith("/")) {
            resourceFolderPath = "/" + resourceFolderPath;
        }
        URL resourceUrl = Application.class.getResource(resourceFolderPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceFolderPath);
        }
        File resourceFolder = new File(resourceUrl.toURI());
        if (!resourceFolder.exists() || !resourceFolder.isDirectory()) {
            throw new IllegalArgumentException("Resource folder not found or not a directory: " + resourceFolderPath);
        }
        Path tempDir = Files.createTempDirectory("tempResource");
        copyDirectory(resourceFolder, tempDir.toFile());
        registerDeleteOnExit(tempDir.toFile());
        return tempDir;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            for (String child : Objects.requireNonNull(source.list())) {
                copyDirectory(new File(source, child), new File(target, child));
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void registerDeleteOnExit(File file) {
        file.deleteOnExit();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    registerDeleteOnExit(child);
                }
            }
        }
    }
}
