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

    /**
     * This method sets the static stage reference to the passed stage parameter.
     * @param newStage Stage object to set as the static stage reference
     */
    public static void setStage(Stage newStage) {
        SceneManager.stage = newStage;
    }

    /**
     * Switches the current scene to the one specified by the passed FXML path.
     * @param FXMLPath Filepath to the FXML file to load
     * @throws IOException Occurs if the FXML file cannot be loaded
     */
    public static void switchScene(String FXMLPath) throws IOException {
        // Load the passed FXML file and show it
        Parent root = FXMLLoader.load(SceneManager.class.getResource(FXMLPath));
        Scene newScene = new Scene(root);
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
