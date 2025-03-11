package io.github.intisy.rsync;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties properties;
    private final File configFile;

    private Config(Properties properties, File configFile) {
        this.properties = properties;
        this.configFile = configFile;
    }

    public static Config parse(File configFile) {
        if (!configFile.exists()) {
            writeTemplate(configFile);
        }
        Properties props = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            props.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Config(props, configFile);
    }

    public static void writeTemplate(File configPath) {
        Properties props = new Properties();
        writeProperties(configPath, props);
    }

    public static void writeProperties(File configPath, Properties properties) {
        try (FileWriter writer = new FileWriter(configPath)) {
            properties.store(writer, "RSync Config Template");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRemoteA() {
        return properties.getProperty("remoteA", "<<MUST SPECIFY>>");
    }

    public String getRemoteB() {
        return properties.getProperty("remoteB", "<<MUST SPECIFY>>");
    }

    public String getRemotePath() {
        return getRemoteA().endsWith(":") ? getRemoteA() : getRemoteB();
    }

    public String getPath() {
        return getRemoteB().endsWith(":") ? getRemoteA() : getRemoteB();
    }

    public void setPath(String path) {
        properties.setProperty(getRemoteB().endsWith(":") ? "remoteA" : "remoteB", path);
        writeProperties(configFile, properties);
    }

    public void setRemotePath(String path) {
        properties.setProperty(getRemoteA().endsWith(":") ? "remoteA" : "remoteB", path);
        writeProperties(configFile, properties);
    }

    public boolean isDryRun() {
        return Boolean.parseBoolean(properties.getProperty("dryRun", "false"));
    }

    public void setDryRun(boolean dryRun) {
        properties.setProperty("dryRun", String.valueOf(dryRun));
        writeProperties(configFile, properties);
    }

    public boolean isActive() {
        return Boolean.parseBoolean(properties.getProperty("active", "false"));
    }

    public void setActive(boolean active) {
        properties.setProperty("active", String.valueOf(active));
        writeProperties(configFile, properties);
    }

    public int getMaxDelete() {
        return Integer.parseInt(properties.getProperty("maxDelete", "50"));
    }

    public void setMaxDelete(int maxDelete) {
        properties.setProperty("maxDelete", String.valueOf(maxDelete));
        writeProperties(configFile, properties);
    }
}