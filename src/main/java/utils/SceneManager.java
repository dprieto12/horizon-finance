package utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import javafx.application.Platform;
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
     * <p>Smallest size the window can be resized to, in pixels, including the window frame.</p>
     *
     *<p>Now that every scene lays itself out with containers rather than fixed coordinates, the point at which content
     *  actually stops fitting is set by the content itself. Measured across the six scenes, the largest layout minimums
     *  are 505px wide (the list plus fixed-width form in transactionOptions) and 629px tall (the two stacked panels in
     *  accountSettings, the one scene whose body does not scroll).</p>
     *
     * <p>These values clear both once the window frame is accounted for, which costs roughly 16px of width and 39px of
     * height on Windows. Width has headroom beyond the minimum so the four summary tiles in analyticsDashboard still
     * read comfortably rather than merely avoiding a clip.</p>
     */
    private static final double MIN_WIDTH = 720;
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
        
        // Apply dark title bar on Windows after the stage is shown
        if (isWindows()) {
            Platform.runLater(() -> setDarkTitleBar(stage));
        }
    }

    /**
     * Returns the static stage reference.
     * @return The current Stage object
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * <p>Switches the current scene to the one specified by the passed FXML path and loads CSS styling for the scene.</p>
     *
     * <p>Note: When using this method within other classes, be sure to pass the full path to the FXML file, not just
     * the filename.</p>
     *
     * <p>For Example: FXMLPath = "/fxml/dashboard.fxml" instead of "dashboard.fxml"</p>
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
     * <p>Sets the title of the window to the passed newTitle parameter to correspond to the current page.</p>
     * <p>Includes the application name "Horizon Finance" in the title, so the full title is "newPage - Horizon Finance".</p>
     * @param newPage Title of the new page / scene.
     */
    public static void setTitle(String newPage) {
        stage.setTitle(newPage + " - Horizon Finance");
    }



    // Windows Title Bar Dark Mode Methods

    /*
    In order to set the title bar as black for Windows users, the native Windows API is used, which the below methods
    relate to or implement.
     */

    /**
     * Checks if the application is running on Windows.
     * @return true if the application is running on Windows, false otherwise
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * <p>Sets the Windows title bar to dark mode using native Windows APIs.</p>
     *
     * <p>Note: This is a public method, so it can be called on Alert dialogs as well.</p>
     */
    public static void setDarkTitleBar(javafx.stage.Window window) {
        // Do not set dark title bar on non-Windows
        if (!isWindows()) {
            return;
        }

        // If on Windows...
        try {
            System.out.println("Attempting to set dark title bar...");
            // Get the native window handle
            WinDef.HWND hwnd = getNativeWindowHandle(window);
            
            if (hwnd != null) {
                System.out.println("Found window handle: " + hwnd);
                // Use Windows API to set dark mode
                int result = DwmAPI.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    20, // DWMWA_USE_IMMERSIVE_DARK_MODE
                    new IntByReference(1),
                    4
                );
                System.out.println("DwmSetWindowAttribute result: " + result + " (0 = success)");
            } else {
                System.err.println("Could not get window handle");
            }
        } catch (Exception e) {
            System.err.println("Failed to set dark title bar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the Windows title bar to dark mode using native Windows APIs.
     */
    private static void setDarkTitleBar(Stage stage) {
        setDarkTitleBar((javafx.stage.Window) stage);
    }

    /**
     * Gets the native window handle for a JavaFX Window.
     */
    private static WinDef.HWND getNativeWindowHandle(javafx.stage.Window window) {
        try {
            // Use User32 to find the window by title
            String title = null;
            if (window instanceof Stage) {
                title = ((Stage) window).getTitle();
            }
            System.out.println("Looking for window with title: " + title);
            if (title == null || title.isEmpty()) {
                System.err.println("Window title is null or empty");
                return null;
            }
            
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            if (hwnd != null) {
                System.out.println("Found window by title");
            } else {
                System.err.println("Could not find window by title, trying class name...");
                // Try by class name as fallback
                hwnd = User32.INSTANCE.FindWindow("SunAwtFrame", null);
                if (hwnd != null) {
                    System.out.println("Found window by class name");
                }
            }
            return hwnd;
        } catch (Exception e) {
            System.err.println("Failed to get native window handle: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Interface for Windows DWM API.
     */
    private interface DwmAPI extends StdCallLibrary {
        DwmAPI INSTANCE = Native.load("dwmapi", DwmAPI.class);
        
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int attr, IntByReference attrValue, int attrSize);
    }
}
