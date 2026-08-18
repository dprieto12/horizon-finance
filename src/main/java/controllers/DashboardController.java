package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;


// TODO: Stylize
// TODO: Add button to go back to account selection

public class DashboardController {
    @FXML
    private Label welcomeBackLabel;

    @FXML
    private Label accountBalanceLabel;

    @FXML
    public void initialize() {
        updateWelcomeLabel();
        updateAccountBalanceLabel();
    }

    private void updateWelcomeLabel() {
        String customWelcome = "Welcome back, " + ApplicationState.getCurrentAccount().getFirstName() + "!";
        welcomeBackLabel.setText(customWelcome);
    }

    private void updateAccountBalanceLabel() {
        String customBalance = "Account Balance: $" +
                String.format("%.2f", ApplicationState.getCurrentAccount().getBalance());
        accountBalanceLabel.setText(customBalance);
    }

    public void navigateToAccounts() throws IOException {
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
        ApplicationState.setCurrentAccount(null);
    }

    public void viewTransactions() throws IOException {
        SceneManager.switchScene("/fxml/transactionOptions.fxml");
    }

    public void viewAnalytics() throws IOException {
        SceneManager.switchScene("/fxml/viewAnalytics.fxml");
    }

    public void viewAccount() throws IOException {
        SceneManager.switchScene("/fxml/accountSettings.fxml");
    }
}
