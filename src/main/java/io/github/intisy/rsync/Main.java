package io.github.intisy.rsync;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

public class Main {
    private static final boolean logToFile = false;
    public static void main(String[] args) {
        File logFile = new File(Rclone.getPath(), "latest.log");
        try {
            if (logToFile && logFile.exists() && !logFile.delete())
                throw new RuntimeException("Could not delete log file " + logFile);
            if (logFile.createNewFile() && logToFile) {
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
