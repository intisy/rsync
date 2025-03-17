package io.github.intisy.rsync;

import io.github.intisy.rsync.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Rclone {
    private final File rcloneFile;
    private final Config config;

    public Rclone() {
        this(getConfig());
    }

    public Rclone(Config config) {
        this(getRcloneFile(), config);
    }

    public Rclone(File rcloneFile) {
        this(rcloneFile, getConfig());
    }

    public Rclone(File rcloneFile, Config config) {
        this.rcloneFile = rcloneFile;
        this.config = config;
    }

    public static File getRcloneFile() {
        return extractRcloneBinary(getPath().toPath().resolve("rclone")).resolve("rclone.exe").toFile();
    }

    public static File getPath() {
        return new File(System.getProperty("user.home") + "/.rsync");
    }

    public static Config getConfig() {
        return Config.parse(new File(getPath(), "config.properties"));
    }

    private static Path extractRcloneBinary(Path rcloneBinary) {
        if (!rcloneBinary.toFile().exists())
            try {
                FileUtils.copyResourceFolder("/rclone", rcloneBinary);
                System.out.println("Extracted rclone binary to: " + rcloneBinary.toFile().getAbsolutePath());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Error extracting rclone binary");
            }
        return rcloneBinary;
    }

    public void resync() throws IOException, InterruptedException {
        System.out.println("Starting bi-directional resync...");
        String remotePath = config.getRemotePath();
        String path = config.getPath();

        if (remotePath.equals("<<MUST SPECIFY>>") || path.equals("<<MUST SPECIFY>>")) {
            throw new IllegalArgumentException("Please specify both remoteA and remoteB in the config.");
        }

        runRCloneCommand("bisync", remotePath + "/", path + "/", config.isDryRun(), "--create-empty-src-dirs", "--compare", "size,modtime,checksum", "--slow-hash-sync-only", "--max-delete", config.getMaxDelete(), "--resilient", "-MvP", "--drive-skip-gdocs", "--fix-case", "--resync");

        System.out.println("Resync complete.");
    }

    public void sync() throws IOException, InterruptedException {
        System.out.println("Starting bi-directional sync...");
        String remotePath = config.getRemotePath();
        String path = config.getPath();

        if (remotePath.equals("<<MUST SPECIFY>>") || path.equals("<<MUST SPECIFY>>")) {
            throw new IllegalArgumentException("Please specify both remoteA and remoteB in the config.");
        }

        String output = runRCloneCommand("bisync", remotePath + "/", path + "/", config.isDryRun(), "--create-empty-src-dirs", "--compare", "size,modtime,checksum", "--slow-hash-sync-only", "--max-delete", config.getMaxDelete(), "--resilient", "-MvP", "--drive-skip-gdocs", "--fix-case");
        if (output.contains("(fatal error encountered)"))
            resync();

        System.out.println("Synchronization complete.");
    }

    private String runRCloneCommand(String command, String src, String dest, boolean dryRun, Object... options)
            throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(rcloneFile.getAbsolutePath());
        cmd.add(command);
        cmd.add(src);
        cmd.add(dest);
        for (Object option : options)
            cmd.add(option.toString());
        if (dryRun) {
            cmd.add("--dry-run");
        }
        System.out.println("Running command: " + String.join(" ", cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        
        // Redirect error stream to output stream
        pb.redirectErrorStream(true);
        
        // Start the process
        Process process = pb.start();
        
        // Capture the output
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
        
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            System.out.println(line); // Still print output to console
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("rclone command exited with code " + exitCode);
        }
        
        return output.toString();
    }

    public boolean isResponsive(String apiUrl) {
        return getResponse(apiUrl) == 401;
    }
    public int getResponse(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestMethod("GET");
            System.out.println("Got response from server: " + connection.getResponseCode());
            return connection.getResponseCode();
        } catch (Exception e) {
            return -1;
        }
    }

    public Process launchRcloneAPI() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                rcloneFile.getAbsolutePath(),
                "rcd",
                "--rc-web-gui",
                "--rc-addr", "127.0.0.1:5572"
        );
        pb.inheritIO();
        System.out.println("Starting rclone API...");
        return pb.start();
    }
}
