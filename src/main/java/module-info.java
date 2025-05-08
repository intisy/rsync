module rsync.main {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;
    requires jdk.zipfs;
    requires com.google.gson;
    requires java.net.http;
    requires org.fusesource.jansi;

    exports io.github.intisy.rsync to javafx.graphics;
}
