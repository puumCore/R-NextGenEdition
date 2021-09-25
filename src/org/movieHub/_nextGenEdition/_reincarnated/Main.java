package org.movieHub._nextGenEdition._reincarnated;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class Main extends Application {

    public static File RESOURCE_PATH = new File(System.getenv("JAVAFX_DEV_APP_HOME").concat("\\_movie_hub_v2\\_next_gen_edition"));
    public static Stage stage;
    public static Process process;
    private double xOffset, yOffset;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/sample.fxml")));
        Scene scene = new Scene(root);
        scene.setOnMousePressed(event2 -> {
            xOffset = event2.getSceneX();
            yOffset = event2.getSceneY();
        });
        scene.setOnMouseDragged(event1 -> {
            primaryStage.setX(event1.getScreenX() - xOffset);
            primaryStage.setY(event1.getScreenY() - yOffset);
        });
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/_images/_logo/myLogo.png")).toExternalForm()));
        primaryStage.setTitle("Next Gen Edition");

        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> {
            if (Main.process != null) {
                Main.process.destroy();
            }
            System.exit(0);
        });

        Main.stage = primaryStage;
    }
}
