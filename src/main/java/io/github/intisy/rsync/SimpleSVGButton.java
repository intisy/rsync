package io.github.intisy.rsync;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SimpleSVGButton extends Pane {
    protected final Rectangle rectangle;
    private final Property<Color> backgroundColorProperty;
    private final Property<Color> hoverColorProperty;
    private final Property<Color> deactivatedbackgroundColorProperty;
    private final DoubleProperty widthProperty;
    private final DoubleProperty heightProperty;
    public boolean enabled = true;

    public SimpleSVGButton(Node group, double width, double height) {
        double multiplier = Math.min(height, width)/40;
        rectangle = new Rectangle(width, height);
        hoverColorProperty = new SimpleObjectProperty<>();
        this.widthProperty = new SimpleDoubleProperty();
        widthProperty.addListener((observableValue, oldValue, newValue) -> {
            rectangle.setWidth(newValue.doubleValue());
            super.setWidth(newValue.doubleValue());
        });
        this.heightProperty = new SimpleDoubleProperty();
        heightProperty.addListener((observableValue, oldValue, newValue) -> {
            rectangle.setHeight(newValue.doubleValue());
            super.setHeight(newValue.doubleValue());
        });
        backgroundColorProperty = new SimpleObjectProperty<>();
        backgroundColorProperty.addListener((observableValue, oldValue, newValue) -> rectangle.setFill(newValue));
        deactivatedbackgroundColorProperty = new SimpleObjectProperty<>();
        deactivatedbackgroundColorProperty.addListener((observableValue, oldValue, newValue) -> {
            if (!enabled)
                rectangle.setFill(newValue);
        });

        rectangle.setFill(getBackgroundColor());
        rectangle.setLayoutX(0);
        rectangle.setLayoutY(0);
        getChildren().addAll(rectangle, group);

        setOnMouseEntered(this::invokeOnMouseEntered);
        setOnMouseExited(this::invokeOnMouseExited);
        setOnMouseClicked(this::invokeOnClick);
        setBackgroundColor(Color.TRANSPARENT);

        addOnMouseEntered(() -> rectangle.setFill(getHoverColor()));
        addOnMouseExited(() -> rectangle.setFill(getBackgroundColor()));
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, null);
    }

    public void setEnabled(boolean enabled, String message) {
        this.enabled = enabled;
        if (enabled && backgroundColorProperty.getValue() != null) {
            rectangle.setFill(backgroundColorProperty.getValue());
        } else if (deactivatedbackgroundColorProperty.getValue() != null) {
            rectangle.setFill(deactivatedbackgroundColorProperty.getValue());
        }
    }

    public void setHoverColor(Color color) {
        hoverColorProperty.setValue(color);
    }

    public Color getHoverColor() {
        return hoverColorProperty.getValue();
    }

    public Property<Color> getHoverColorProperty() {
        return hoverColorProperty;
    }

    public void setDeactivatedBackgroundColor(Color color) {
        deactivatedbackgroundColorProperty.setValue(color);
    }

    public Color getDeactivatedbackgroundColor() {
        return deactivatedbackgroundColorProperty.getValue();
    }

    public Property<Color> getDeactivatedbackgroundColorProperty() {
        return deactivatedbackgroundColorProperty;
    }

    public void setBackgroundColor(Color color) {
        backgroundColorProperty.setValue(color);
    }

    public Color getBackgroundColor() {
        return backgroundColorProperty.getValue();
    }

    public Property<Color> getBackgroundColorProperty() {
        return backgroundColorProperty;
    }

    public DoubleProperty getHeightProperty() {
        return heightProperty;
    }

    public DoubleProperty getWidthProperty() {
        return widthProperty;
    }

    public List<Runnable> onMouseEntered = new ArrayList<>();

    public final void addOnMouseEntered(Runnable action) {
        onMouseEntered.add(action);
    }

    public final void invokeOnMouseEntered(MouseEvent mouseEvent) {
        invokeOnMouseEntered();
    }

    public final void invokeOnMouseEntered() {
        for (Runnable action : onMouseEntered) {
            action.run();
        }
    }

    public List<Runnable> onMouseExited = new ArrayList<>();

    public final void addOnMouseExited(Runnable action) {
        onMouseExited.add(action);
    }

    public final void invokeOnMouseExited(MouseEvent mouseEvent) {
        invokeOnMouseExited();
    }

    public final void invokeOnMouseExited() {
        for (Runnable action : onMouseExited) {
            action.run();
        }
    }

    public List<Runnable> onClick = new ArrayList<>();

    public final void addOnClick(Runnable action) {
        onClick.add(action);
    }

    public final void invokeOnClick(MouseEvent mouseEvent) {
        invokeOnClick();
    }

    public final void invokeOnClick() {
        for (Runnable action : onClick) {
            action.run();
        }
    }
}
