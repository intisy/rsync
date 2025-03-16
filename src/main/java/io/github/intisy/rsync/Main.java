package io.github.intisy.rsync;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final boolean debug = false;
    public static void main(String[] args) {
        File logFile = new File(Rclone.getPath(), "latest.log");
        try {
            if (!debug && logFile.exists() && !logFile.delete())
                throw new RuntimeException("Could not delete log file " + logFile);
            if (logFile.createNewFile() && !debug) {
                System.setErr(new java.io.PrintStream(logFile));
                System.setOut(new java.io.PrintStream(logFile));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Application.launch(Application.class, args);
    }
}
