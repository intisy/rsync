package io.github.intisy.rsync;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Main {
    private static final boolean logToFile = false;
    public static void main(String[] args) {
        File rclonePath = Rclone.getPath();
        File logFile = new File(rclonePath, "latest.log");

        if (!rclonePath.exists() && !rclonePath.mkdirs())
            throw new RuntimeException("Failed to create directory: " + rclonePath);

        try {
            if (logToFile && logFile.exists() && !logFile.delete())
                throw new RuntimeException("Could not delete log file " + logFile);
            if (logToFile && logFile.createNewFile()) {
                PrintStream systemOut = System.out;
                PrintStream systemErr = System.err;
                System.setOut(new PrintStream(logFile) {
                    @Override
                    public void println(String x) {
                        super.println(x);
                        systemOut.println(x);
                    }
                });
                System.setErr(new PrintStream(logFile) {
                    @Override
                    public void println(String x) {
                        super.println(x);
                        systemErr.println(x);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Application.launch(Application.class, args);
    }
}
