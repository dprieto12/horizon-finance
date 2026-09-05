package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;

public class DashboardController {
    // Dynamic labels within the scene
    @FXML
    private Label welcomeBackLabel;

    @FXML
    private Label accountBalanceLabel;

    /**
     * Initializes the controller by setting the title and updating the labels.
     */
    @FXML
    public void initialize() {
        SceneManager.setTitle("Your Dashboard");

        updateWelcomeLabel();
        updateAccountBalanceLabel();
    }

    /**
     * Updates the welcome label with the current account's first name.
     */
    private void updateWelcomeLabel() {
        String customWelcome = "Welcome back, " + ApplicationState.getCurrentAccount().getFirstName() + "!";
        welcomeBackLabel.setText(customWelcome);
    }

    /**
     * Updates the account balance label with the current account's balance.
     */
    private void updateAccountBalanceLabel() {
        String customBalance = "Account Balance: $" +
                String.format("%.2f", ApplicationState.getCurrentAccount().getBalance());
        accountBalanceLabel.setText(customBalance);
    }

    /**
     * Navigates the user back to the account selection screen.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void navigateToAccounts() throws IOException {
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
        ApplicationState.setCurrentAccount(null);
    }

    /**
     * Navigates the user to the transaction options screen.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void viewTransactions() throws IOException {
        SceneManager.switchScene("/fxml/transactionOptions.fxml");
    }

    /**
     * Navigates the user to the analytics dashboard.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void viewAnalytics() throws IOException {
        SceneManager.switchScene("/fxml/analyticsDashboard.fxml");
    }

    /**
     * Navigates the user to the account settings screen.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void viewAccount() throws IOException {
        SceneManager.switchScene("/fxml/accountSettings.fxml");
    }
}
