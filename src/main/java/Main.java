import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class handles the main application flow and includes startup tasks to initialize the application. In specific,
 * it loads the application window utilizing the SceneManager class and sets up the database using the DatabaseManager
 * class, then allowing the Controller classes to handle execution on a scene-by-scene basis after.
 */

// TODO: Upon implementation of the SceneManager class alongside any other necessary classes, revise the start()
//  method to use all necessary helper methods.

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("chooseAccount.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();    }
}
