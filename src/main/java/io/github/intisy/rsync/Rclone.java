package io.github.intisy.rsync;

import io.github.intisy.rsync.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

@SuppressWarnings("SameParameterValue")
public class Rclone {
    private boolean useApi;
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

    public void setUseApi(boolean useApi) {
        this.useApi = useApi;
    }

    public static File getRcloneFile() {
        return extractRcloneBinary(getRclonePath().toPath()).resolve(OsDetector.getOperatingSystemType() == OsDetector.OSType.WINDOWS ? "rclone.exe" : "rclone").toFile();
    }

    public static File getPath() {
        return new File(System.getProperty("user.home") + "/.rsync");
    }

    public static File getRclonePath() {
        return new File(getPath(), "rclone/" + OsDetector.getOperatingSystemType().name().toLowerCase());
    }

    public static Config getConfig() {
        return Config.parse(new File(getPath(), "config.properties"));
    }

    private static Path extractRcloneBinary(Path rcloneBinary) {
        if (!rcloneBinary.toFile().exists())
            try {
                if (OsDetector.getOperatingSystemType() == OsDetector.OSType.WINDOWS)
                    FileUtils.copyResourceFolder("/rclone/windows", rcloneBinary);
                else if (OsDetector.getOperatingSystemType() == OsDetector.OSType.MAC)
                    FileUtils.copyResourceFolder("/rclone/mac", rcloneBinary);
                else if (OsDetector.getOperatingSystemType() == OsDetector.OSType.LINUX)
                    FileUtils.copyResourceFolder("/rclone/linux", rcloneBinary);
                else
                    throw new RuntimeException("Unsupported OS: " + OsDetector.getRawOsInfo());
                System.out.println("Extracted rclone binary to: " + rcloneBinary.toFile().getAbsolutePath());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Error extracting rclone binary");
            }
        return rcloneBinary;
    }


    public boolean resync() {
        System.out.println("Starting bi-directional resync...");
        if (useApi) {
            String rcUrl = "http://" + getUrl() + "/sync/bisync";

            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            JsonObject payload = getJsonObject();
            payload.addProperty("resync", true);

            String jsonString = new Gson().toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rcUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(BodyPublishers.ofString(jsonString))
                    .build();
            HttpClient client = HttpClient.newHttpClient();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("rclone bisync command executed. Response:");
                System.out.println(response.body());
                if (response.statusCode() == 200) {
                    System.out.println("Synchronization complete.");
                    return true;
                } else
                    System.out.println("Resync failed with status code " + response.statusCode());
            } catch (Exception e) {
                System.err.println("An error occurred while executing the rclone bisync command:");
            }
            return false;
        } else {
            String remotePath = config.getRemotePath();
            String path = config.getPath();

            if (remotePath.equals("<<MUST SPECIFY>>") || path.equals("<<MUST SPECIFY>>")) {
                throw new IllegalArgumentException("Please specify both remoteA and remoteB in the config.");
            }

            try {
                runRCloneCommand("bisync", remotePath + "/", path + "/", config.isDryRun(), "--create-empty-src-dirs", "--compare", "size,modtime,checksum", "--slow-hash-sync-only", "--max-delete", config.getMaxDelete(), "--resilient", "-MvP", "--drive-skip-gdocs", "--fix-case", "--resync");
                System.out.println("Synchronization complete.");
                return true;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static JsonObject getJsonObject() {
        JsonObject payload = new JsonObject();
        payload.addProperty("path1", getConfig().getRemoteA());
        payload.addProperty("path2", getConfig().getRemoteB());
        payload.addProperty("createEmptySrcDirs", true);
        payload.addProperty("compare", "size,modtime,checksum");
        payload.addProperty("slowHashSyncOnly", true);
        payload.addProperty("maxDelete", getConfig().getMaxDelete());
        payload.addProperty("resilient", true);
        payload.addProperty("verbose", true);
        payload.addProperty("progress", true);
        payload.addProperty("driveSkipGdocs", true);
        payload.addProperty("fixCase", true);
        return payload;
    }

    public boolean sync() {
        System.out.println("Starting bi-directional sync...");
        if (useApi) {
            String rcUrl = "http://" + getUrl() + "/sync/bisync";

            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            JsonObject payload = getJsonObject();

            String jsonString = new Gson().toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rcUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(BodyPublishers.ofString(jsonString))
                    .build();
            HttpClient client = HttpClient.newHttpClient();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("rclone bisync command executed. Response:");
                System.out.println(response.body());
                if (response.statusCode() == 200) {
                    System.out.println("Synchronization complete.");
                    return true;
                } else
                    System.out.println("Resync failed with status code " + response.statusCode());
            } catch (Exception e) {
                System.err.println("An error occurred while executing the rclone bisync command:");
            }
            return false;
        } else {
            System.out.println("Starting bi-directional sync...");
            String remotePath = config.getRemotePath();
            String path = config.getPath();

            if (remotePath.equals("<<MUST SPECIFY>>") || path.equals("<<MUST SPECIFY>>")) {
                throw new IllegalArgumentException("Please specify both remoteA and remoteB in the config.");
            }

            try {
                String output = runRCloneCommand(
                        "bisync",
                        remotePath + "/",
                        path + "/",
                        config.isDryRun(),
                        "--create-empty-src-dirs",
                        "--compare", "size,modtime,checksum",
                        "--slow-hash-sync-only",
                        "--max-delete", config.getMaxDelete(),
                        "--resilient",
                        "-MvP",
                        "--checkers", 32,
                        "--fast-list",
                        "--drive-skip-gdocs",
                        "--fix-case"
                );
                if (output.contains("(fatal error encountered)"))
                    return resync();
                System.out.println("Synchronization complete.");
                return true;
            } catch (RuntimeException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String runRCloneCommand(String command, String src, String dest, boolean dryRun, Object... options)
            throws IOException, InterruptedException {
        File cacheFile = new File(rcloneFile.getParentFile(), "cache");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }

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

        pb.directory(cacheFile);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
        
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            System.out.println(line);
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("rclone command exited with code " + exitCode);
        }
        
        return output.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isResponsive(String apiUrl) {
        int response;
        return (response = getResponse(apiUrl)) == 401 || response == 200;
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

    @SuppressWarnings("BusyWait")
    public void waitForApi() {
        while (!isResponsive("http://localhost:5572")) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void launchRcloneApi() throws IOException {
        String[] cmd = {
                rcloneFile.getAbsolutePath(),
                "rcd",
                "--rc-web-gui",
                "--rc-web-gui-no-open-browser",
                "--rc-addr", getUrl()
        };
        if (config.isUsingPassword()) {
            String[] cmd2 = {
                    "--rc-user", config.getUsername(),
                    "--rc-pass", config.getPassword()
            };
            String[] combined = new String[cmd.length + cmd2.length];
            System.arraycopy(cmd, 0, combined, 0, cmd.length);
            System.arraycopy(cmd2, 0, combined, cmd.length, cmd2.length);
            cmd = combined;
        } else {
            String[] combined = new String[cmd.length + 1];
            System.arraycopy(cmd, 0, combined, 0, cmd.length);
            combined[cmd.length] = "--rc-no-auth";
            cmd = combined;
        }
        System.out.println("Running command: " + String.join(" ", cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        System.out.println("Starting rclone API...");
        pb.start();
    }

    public String getUrl() {
        return "127.0.0.1:5572";
    }
}
