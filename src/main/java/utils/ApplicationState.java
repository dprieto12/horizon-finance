package utils;

import models.Account;

import java.util.Stack;

/**
 * The primary purpose of this class is to store the current state of the program for the end-user so that necessary
 * information such as the scene history and current account can be accessed by multiple controllers.
 */

public class ApplicationState {
    private static Account currentAccount;
    private static Stack<String> sceneHistory = new Stack<>();



    // Account related methods

    /**
     * Sets the currentAccount field to the passed Account object.
     * @param account Account object to set as the current account
     */

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    /**
     * Returns the instance of the currently selected account.
     * @return Account object of the currently selected account
     */

    public static Account getCurrentAccount() {
        return currentAccount;
    }


    // Scene history methods

    /**
     * Adds the passed FXML path to the scene history.
     * @param FXMLPath String path to the FXML file to add to the history
     */

    public static void addToHistory(String FXMLPath) {
        sceneHistory.push(FXMLPath);
    }

    /**
     * Returns the previous scene at the top of the sceneHistory stack.
     * @return FXML path of the previous scene
     */

    public static String getPreviousScene() {
        if (sceneHistory.isEmpty()) {
            return null;
        }

        return sceneHistory.pop();
    }

    /**
     * Clears the sceneHistory stack.
      */

    public static void clearHistory() {
        sceneHistory.clear();
    }

    /**
     * Returns true if the sceneHistory stack is not empty.
     * @return Boolean true if the sceneHistory stack is not empty, false otherwise
     */

    public static boolean hasHistory() {
        return !sceneHistory.isEmpty();
    }


}
