package io.github.intisy.rsync;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

@SuppressWarnings({"SameParameterValue", "BusyWait"})
public class Application extends javafx.application.Application {
    private final boolean useApi = false;
    private final int syncDelayInMillis = 0;
    private TrayIcon trayIcon;
    private SystemTray tray;

    @Override
    public void start(Stage primaryStage) {
        double width = 255;
        double height = 240;
        String apiUrl = "http://127.0.0.1:5572/";
        Config config = Rclone.getConfig();
        Rclone rclone = new Rclone(config);
        Wait wait = new Wait();
        wait.setVariable(config.isSync());
        rclone.setUseApi(useApi);
        Platform.setImplicitExit(false);
        primaryStage.setTitle("Rsync");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        InputStream icon = getClass().getResourceAsStream("/icon.png");
        if (icon != null) {
            Image iconImage = new Image(icon);
            primaryStage.getIcons().add(iconImage);
        }

        Title title = new Title(primaryStage, width, 40);

        GridPane grid = new GridPane();
        grid.setMaxWidth(width);
        grid.setMinWidth(width);
        grid.setMinHeight(height);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label remoteLabel = new Label("Remote Path:");
        remoteLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(remoteLabel, 0, 0);

        TextField remotePathField = new TextField();
        remotePathField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
        GridPane.setConstraints(remotePathField, 1, 0);
        remotePathField.textProperty().addListener((observableText, oldText, newText) -> config.setRemotePath(newText));
        remotePathField.setText(config.getRemotePath());

        Label localLabel = new Label("Local Path:");
        localLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(localLabel, 0, 1);

        TextField localPathField = new TextField();
        localPathField.setStyle("-fx-background-color: #3C3C3CFF; -fx-text-fill: white;");
        GridPane.setConstraints(localPathField, 1, 1);
        localPathField.textProperty().addListener((observableText, oldText, newText) -> config.setPath(newText));
        localPathField.setText(config.getPath());

        Label maxDeleteLabel = new Label("Max Delete:");
        maxDeleteLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(maxDeleteLabel, 0, 2);

        TextField maxDeleteField = new TextField();
        maxDeleteField.setStyle("-fx-background-color: #3C3C3CFF; -fx-text-fill: white;");
        GridPane.setConstraints(maxDeleteField, 1, 2);
        maxDeleteField.textProperty().addListener((observableText, oldText, newText) -> config.setMaxDelete(newText.isEmpty() || !newText.matches("^\\d+%?$") ? 0 : Integer.parseInt(newText.split("%")[0])));
        maxDeleteField.setText(config.getMaxDelete() + "%");

        Label dryRunLabel = new Label("Dry Run:");
        dryRunLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(dryRunLabel, 0, 3);

        CheckBox dryRunCheckBox = new CheckBox();
        localPathField.setStyle("-fx-background-color: #3C3C3CFF; -fx-text-fill: white;");
        GridPane.setConstraints(dryRunCheckBox, 1, 3);
        dryRunCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> config.setDryRun(newValue));
        dryRunCheckBox.setSelected(config.isDryRun());

        Label syncLabel = new Label("Sync:");
        syncLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(syncLabel, 0, 4);

        CheckBox syncCheckBox = new CheckBox();
        localPathField.setStyle("-fx-background-color: #3C3C3CFF; -fx-text-fill: white;");
        GridPane.setConstraints(syncCheckBox, 1, 4);
        syncCheckBox.setSelected(config.isSync());
        syncCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            wait.setVariable(newValue);
            config.setSync(newValue);
        });

        Label runLabel = new Label("Run:");
        runLabel.setStyle("-fx-text-fill: white;");
        GridPane.setConstraints(runLabel, 0, 5);

        ToggleButton runToggle = new ToggleButton("OFF");
        runToggle.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        runToggle.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            config.setActive(runToggle.isSelected());
            if (runToggle.isSelected()) {
                runToggle.setText("ON");
                runToggle.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                new Thread(() -> {
                    try {
                        if (!rclone.isResponsive(apiUrl))
                            rclone.launchRcloneApi();
                        rclone.waitForApi();

                        File lockPath = new File(System.getenv("LOCALAPPDATA") + "\\rclone\\bisync\\");
                        if (lockPath.exists()) {
                            for (File file : Objects.requireNonNull(lockPath.listFiles())) {
                                if (file.getName().contains(config.getRemotePath().split(":")[0]))
                                    if (file.getName().contains(config.getPath().replace(":", "_").replace("\\", "_")))
                                        if (file.delete())
                                            System.out.println("Deleted lock file: " + file.getName());
                            }
                        }

                        boolean shouldResync = syncCheckBox.isSelected() && !rclone.resync();
                        while (config.isActive()) {
                            if (!wait.getVariable()) {
                                wait.waitForVariable(true);
                                shouldResync = rclone.resync();
                            }
                            if (shouldResync)
                                shouldResync = rclone.resync();
                            else
                                shouldResync = rclone.sync();
                            Thread.sleep(syncDelayInMillis);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
            } else {
                runToggle.setText("OFF");
                runToggle.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
            }
        });
        GridPane.setConstraints(runToggle, 1, 5);
        runToggle.setSelected(config.isActive());

        Pane pane = new Pane();
        grid.getChildren().addAll(remoteLabel, remotePathField, localLabel, localPathField, maxDeleteLabel, maxDeleteField, dryRunLabel, dryRunCheckBox, syncLabel, syncCheckBox, runLabel, runToggle);
        grid.setLayoutY(40);
        pane.getChildren().addAll(title, grid);
        pane.setStyle("-fx-background-color: #2b2b2b;");

        Scene scene = new Scene(pane, width, height);
        primaryStage.setScene(scene);
        setupSystemTray(primaryStage);
        EventHandler<WindowEvent> closeEvent = event -> {
            if (event != null)
                event.consume();
            hideToSystemTray(primaryStage);
        };
        primaryStage.setOnCloseRequest(closeEvent);
        System.out.println("Launching with params: " + getParameters().getRaw());

        if (getParameters().getRaw().contains("/auto"))
            closeEvent.handle(null);
        else
            primaryStage.show();
    }

    private void setupSystemTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            return;
        }

        tray = SystemTray.getSystemTray();
        try {
            URL imageURL = getClass().getResource("/icon.png"); // Replace with your tray icon path
            assert imageURL != null;
            java.awt.Image image = ImageIO.read(imageURL);
            trayIcon = new TrayIcon(image, "Sync UI");

            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Platform.runLater(() -> showFromSystemTray(stage));
                    }
                }
            });

            PopupMenu popup = getPopupMenu(stage);
            trayIcon.setPopupMenu(popup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PopupMenu getPopupMenu(Stage stage) {
        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> {
            Platform.runLater(() -> {
                stage.show();
                stage.toFront();
            });
            tray.remove(trayIcon);
        });
        popup.add(openItem);

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            Platform.exit();
            System.exit(0);
        });
        popup.add(exitItem);
        return popup;
    }

    private void hideToSystemTray(Stage stage) {
        Platform.runLater(() -> {
            if (tray != null && trayIcon != null) {
                try {
                    tray.add(trayIcon);
                    stage.hide();
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void showFromSystemTray(Stage stage) {
        Platform.runLater(() -> {
            tray.remove(trayIcon);
            stage.show();
            stage.toFront();
        });
    }
}
