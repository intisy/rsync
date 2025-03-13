module rsync.main {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;
    requires jdk.zipfs;

    exports io.github.intisy.rsync to javafx.graphics;
}
