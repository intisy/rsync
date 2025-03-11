package io.github.intisy.rsync;

import io.github.intisy.rsync.utils.JfxUtils;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.*;
import java.io.InputStream;

public class Title extends Pane {
    double iconSize = 10;
    public Title(Stage stage, double width, double height) {
        final Double[] clickPoint = new Double[2];
        double buttonWidth = 50;
        javafx.scene.control.Label blizzityLabel = new javafx.scene.control.Label(stage.getTitle());
        blizzityLabel.setFont(javafx.scene.text.Font.font(blizzityLabel.getFont().getFamily(), FontWeight.BOLD, blizzityLabel.getFont().getSize() * 1.3));
        blizzityLabel.setTextFill(Color.WHITE);
        blizzityLabel.setLayoutX(38);
        blizzityLabel.setLayoutY(9);
        Rectangle background = new Rectangle(width, height);
        setOnMousePressed(event -> {
            clickPoint[0] = event.getScreenX();
            clickPoint[1] = event.getScreenY();
        });
        setOnMouseDragged((event) -> {
            double x = stage.getX() - (clickPoint[0] - event.getScreenX());
            double y = stage.getY() - (clickPoint[1] - event.getScreenY());
            clickPoint[0] = event.getScreenX();
            clickPoint[1] = event.getScreenY();
            stage.setX(x);
            stage.setY(y);
        });
        Group closeShape = new Group(
                new Line(0, 0, iconSize, iconSize),
                new Line(0, iconSize, iconSize, 0)
        );
        closeShape.setLayoutX((50 - iconSize) / 2);
        closeShape.setLayoutY((height - iconSize) / 2);
        closeShape.getChildren().forEach(line -> ((Line) line).setStroke(Color.WHITE));
        SimpleSVGButton closeButton = new SimpleSVGButton(closeShape, 50, height);
        closeButton.setHoverColor(Color.rgb(201,79,79));
        closeButton.addOnClick(() -> stage.getOnCloseRequest().handle(null));
        Rectangle maximizeRectangle = new Rectangle(iconSize, iconSize);
        maximizeRectangle.setStrokeWidth(1);
        double arc = iconSize / 4;
        double offset = iconSize / 3;
        maximizeRectangle.setArcWidth(arc);
        maximizeRectangle.setArcHeight(arc);
        maximizeRectangle.setY(height / 2 - iconSize / 2);
        maximizeRectangle.setX(buttonWidth / 2 - iconSize / 2);
        maximizeRectangle.setFill(Color.TRANSPARENT);
        maximizeRectangle.setStroke(Color.WHITE);
        int offsetY = (int) (height / 2 - (iconSize + offset) / 2 + offset);
        int offsetX = (int) (buttonWidth / 2 - (iconSize + offset) / 2);
        Rectangle frontRect = new Rectangle(iconSize, iconSize);
        frontRect.setFill(Color.TRANSPARENT);
        frontRect.setArcWidth(arc);
        frontRect.setArcHeight(arc);
        frontRect.setLayoutX(offsetX);
        frontRect.setLayoutY(offsetY);
        frontRect.setStroke(Color.WHITE);
        frontRect.setStrokeWidth(1);
        Path backPath = new Path();
        backPath.getElements().add(new MoveTo(offset, - offset));
        backPath.getElements().add(new LineTo(iconSize + offset - arc, - offset));
        backPath.getElements().add(new ArcTo(arc, arc, 90, iconSize + offset, arc - offset, false, true));
        backPath.getElements().add(new LineTo(iconSize + offset, iconSize - offset));
        backPath.setStroke(Color.WHITE);
        backPath.setLayoutX(offsetX);
        backPath.setLayoutY(offsetY);
        backPath.setStrokeWidth(1);
        Group unMaximizeShape = new Group(backPath, frontRect);
        JfxUtils.snapToPixel(maximizeRectangle);
        Pane sizeShape = new Pane(maximizeRectangle);
        SimpleSVGButton sizeButton = new SimpleSVGButton(sizeShape, buttonWidth, height);
        sizeButton.setHoverColor(Color.rgb(72,75,77));
        sizeButton.addOnClick(() -> {
            if (stage.isMaximized()) {
                JfxUtils.snapToPixel(maximizeRectangle);
                sizeShape.getChildren().remove(unMaximizeShape);
                sizeShape.getChildren().add(maximizeRectangle);
            } else {
                Point2D snap = JfxUtils.snapToPixel(frontRect);
                JfxUtils.snapToPixel(backPath, snap);
                sizeShape.getChildren().add(unMaximizeShape);
                sizeShape.getChildren().remove(maximizeRectangle);
            }
            stage.setMaximized(!stage.isMaximized());
        });
        Line minimizeShape = new Line(0, 0, iconSize, 0);
        minimizeShape.setLayoutX((buttonWidth - iconSize) / 2);
        minimizeShape.setLayoutY(height / 2);
        minimizeShape.setStroke(Color.WHITE);
        minimizeShape.setStrokeWidth(1);
        JfxUtils.snapToPixel(minimizeShape);
        SimpleSVGButton minimizeButton = new SimpleSVGButton(minimizeShape, buttonWidth, height);
        minimizeButton.setHoverColor(Color.rgb(72,75,77));
        minimizeButton.addOnClick(() -> stage.setIconified(true));
        background.setFill(Color.rgb(60,63,65));
        InputStream icon = getClass().getResourceAsStream("/icon.png");
        assert icon != null;
        double size = 0.1;
        Image image = new Image(icon);
        ImageView iconImage = new ImageView(image);
        iconImage.setScaleX(size);
        iconImage.setScaleY(size);
        getChildren().addAll(background, blizzityLabel, iconImage, minimizeButton, sizeButton, closeButton);
        closeButton.setLayoutX((width - buttonWidth));
        sizeButton.setLayoutX((width - buttonWidth * 2));
        minimizeButton.setLayoutX((width - buttonWidth * 3));
        iconImage.setLayoutX(-image.getWidth() / 2 + 20);
        iconImage.setLayoutY(-image.getHeight() / 2 + 20);
    }
}
