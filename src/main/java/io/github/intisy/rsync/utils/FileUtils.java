package io.github.intisy.rsync.utils;

import io.github.intisy.rsync.Application;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

public class FileUtils {
    public static Path copyResourceFolderToTemp(String resourceFolderPath)
            throws IOException, URISyntaxException {
        if (!resourceFolderPath.startsWith("/")) {
            resourceFolderPath = "/" + resourceFolderPath;
        }

        URL resourceUrl = Application.class.getResource(resourceFolderPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceFolderPath);
        }

        Path tempDir = Files.createTempDirectory("tempResource");
        URI resourceUri = resourceUrl.toURI();

        if ("jar".equals(resourceUri.getScheme())) {
            try (FileSystem fs = FileSystems.newFileSystem(resourceUri, Collections.emptyMap())) {
                Path jarPath = fs.getPath(resourceFolderPath);
                Files.walk(jarPath).forEach(source -> {
                    try {
                        Path relativePath = jarPath.relativize(source);
                        Path targetPath = tempDir.resolve(relativePath.toString());
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(targetPath);
                        } else {
                            Files.copy(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } else {
            File resourceFolder = new File(resourceUri);
            if (!resourceFolder.exists() || !resourceFolder.isDirectory()) {
                throw new IllegalArgumentException("Resource folder not found or not a directory: " + resourceFolderPath);
            }
            copyDirectory(resourceFolder, tempDir.toFile());
        }

        // Optionally register the temporary directory for deletion on exit
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
