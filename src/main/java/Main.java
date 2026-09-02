import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import utils.SceneManager;
import database.DatabaseManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class handles the main application flow and includes startup tasks to initialize the application. In specific,
 * it loads the application window utilizing the SceneManager class and sets up the database using the DatabaseManager
 * class, then allowing the Controller classes to handle execution on a scene-by-scene basis after.
 */

// TODO: Upon implementation of the SceneManager class alongside any other necessary classes, revise the start()
//  method to use all necessary helper methods.

public class Main extends Application {

    /**
     * Classpath locations of the Inter faces that styles.css relies on. All three declare the same internal
     * family name ("Inter 18pt"), which is what lets CSS reach the bold and italic faces through
     * -fx-font-weight and -fx-font-style rather than needing a separate family per face.
     */
    private static final String[] FONT_RESOURCES = {
            "/fonts/Inter/static/Inter_18pt-Regular.ttf",
            "/fonts/Inter/static/Inter_18pt-Bold.ttf",
            "/fonts/Inter/static/Inter_18pt-Italic.ttf"
    };

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        DatabaseManager.getInstance();

        // Register fonts before any scene is built, so the first scene already renders in Inter
        loadFonts();

        // Set stylizing
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        // Set window icon
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/HorizonIcon.jpg")));

        // Set stage instance and load the initial scene
        SceneManager.setStage(stage);
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
    }

    /**
     * Registers the Inter font faces with the JavaFX font system. JavaFX CSS has no working @font-face rule, so
     * a font bundled in the application's resources is unusable until it is loaded here. A face that fails to
     * load is reported and skipped: the stylesheet then falls back to the next family in its font stack rather
     * than the application failing to start.
     */
    private void loadFonts() {
        for (String fontResource : FONT_RESOURCES) {
            try (InputStream fontStream = Main.class.getResourceAsStream(fontResource)) {
                if (fontStream == null) {
                    System.err.println("Font not found on classpath, skipping: " + fontResource);
                    continue;
                }

                // The size argument only applies to the returned Font object, not to the registration itself
                if (Font.loadFont(fontStream, 12) == null) {
                    System.err.println("Font could not be loaded, skipping: " + fontResource);
                }
            } catch (IOException e) {
                System.err.println("Font could not be read, skipping: " + fontResource + " (" + e.getMessage() + ")");
            }
        }
    }
}
