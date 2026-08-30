package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * This class contains a series of methods that relate to loading and switching scenes within the program, so that
 * multiple classes may make changes to the scene, or pass information into it without repeating code across the
 * program.
 */

public class SceneManager {
    private static Stage stage;

    /** Size of the window when the application first opens, in pixels. */
    private static final double DEFAULT_WIDTH = 1100;
    private static final double DEFAULT_HEIGHT = 700;

    /**
     * Smallest size the window can be resized to, in pixels. Chosen to clear the widest and tallest scene
     * content in the application (the 900x600 layout in transactionOptions.fxml) with room left over for the
     * window frame, so that no scene clips at the minimum size.
     */
    private static final double MIN_WIDTH = 940;
    private static final double MIN_HEIGHT = 680;

    /**
     * This method sets the static stage reference to the passed stage parameter, and applies the window size
     * limits that every scene is laid out against.
     * @param newStage Stage object to set as the static stage reference
     */
    public static void setStage(Stage newStage) {
        SceneManager.stage = newStage;
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    /**
     * Switches the current scene to the one specified by the passed FXML path.
     * @param FXMLPath Filepath to the FXML file to load
     * @throws IOException Occurs if the FXML file cannot be loaded
     */
    public static void switchScene(String FXMLPath) throws IOException {
        // Load the passed FXML file and show it
        Parent root = FXMLLoader.load(SceneManager.class.getResource(FXMLPath));

        // Carry the current window size into the new scene, falling back to the default size on the first
        // load. Building the scene without explicit dimensions would instead size it from the loaded FXML's
        // own preferred size, resizing the window on every navigation and discarding any resize by the user.
        Scene currentScene = stage.getScene();
        double width = (currentScene == null) ? DEFAULT_WIDTH : currentScene.getWidth();
        double height = (currentScene == null) ? DEFAULT_HEIGHT : currentScene.getHeight();

        Scene newScene = new Scene(root, width, height);
        newScene.getStylesheets().add(SceneManager.class.getResource("/styling/styles.css").toExternalForm());
        stage.setScene(newScene);
        stage.show();

    }

    /**
     * Sets the title of the window to the passed newTitle parameter to correspond to the current page.
     * @param newPage Title of the new page / scene.
     */
    public static void setTitle(String newPage) {
        stage.setTitle(newPage + " - Horizon Finance");
    }
}
