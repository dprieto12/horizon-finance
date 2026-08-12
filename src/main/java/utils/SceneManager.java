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
     * Switches the current scene to the one specified by the passed FXML path and sends necessary controller data
     * to the next scene. The controller of the passed scene must implement the DataReceiver interface, which will
     * contain a receiveData() method to handle incoming data.
     * @param FXMLPath Filepath to the FXML file to load
     * @param controllerData Object that must be passed into the next controller using receiveData()
     * @throws IOException Occurs if the FXML file cannot be loaded
     */
    public static void switchScene(String FXMLPath, Object controllerData) throws IOException {
        // Load the FXML file and create a reference to its controller
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(FXMLPath));
        Parent root = loader.load();
        Object controller = loader.getController();

        // If the controller implements DataReceiver, pass the controllerData to it
        if (controller instanceof DataReceiver) {
            ((DataReceiver) controller).receiveData(controllerData);
        }

        // Then, set and show the new scene
        Scene newScene = new Scene(root);
        stage.setScene(newScene);
        stage.show();
    }
}
