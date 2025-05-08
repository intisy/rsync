package io.github.intisy.rsync;

public class OsDetector {
    public enum OSType {
        WINDOWS, MAC, LINUX, SOLARIS, OTHER
    }

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static OSType getOperatingSystemType() {
        if (OS_NAME.contains("win")) {
            return OSType.WINDOWS;
        }
        if (OS_NAME.contains("mac")) {
            return OSType.MAC;
        }
        if (OS_NAME.contains("nux") || OS_NAME.contains("nix")) {
            return OSType.LINUX;
        }
        if (OS_NAME.contains("sunos") || OS_NAME.contains("solaris")) {
            return OSType.SOLARIS;
        }
        return OSType.OTHER;
    }

    public static String getRawOsInfo() {
        String version = System.getProperty("os.version");
        String arch    = System.getProperty("os.arch");
        return String.format("%s %s (%s)", OS_NAME, version, arch);
    }
}
