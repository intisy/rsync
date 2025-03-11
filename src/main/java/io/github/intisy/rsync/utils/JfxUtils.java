package io.github.intisy.rsync.utils;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class JfxUtils {
    public static Image getImage(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return new Image(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void snapToPixel(Node node, Point2D offset) {
        double currentX = node.getLayoutX();
        double currentY = node.getLayoutY();
        node.setLayoutX(currentX + offset.getX());
        node.setLayoutY(currentY + offset.getY());
    }
    public static Point2D snapToPixel(Node node) {
        double currentX = node.getLayoutX();
        double currentY = node.getLayoutY();
        double snappedX = Math.floor(currentX) + 0.5;
        double snappedY = Math.floor(currentY) + 0.5;
        Point2D point2D = new Point2D(currentX-snappedX, currentY-snappedY);
        snapToPixel(node, point2D);
        return point2D;
    }
}
