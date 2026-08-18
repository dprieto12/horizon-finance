package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;


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
        String customBalance = "Account Balance: $" + ApplicationState.getCurrentAccount().getBalance();
        accountBalanceLabel.setText(customBalance);
    }

    public void viewTransactions() throws IOException {
        SceneManager.switchScene("/fxml/transactionOptions.fxml");
    }

    public void viewAnalytics() throws IOException {
        SceneManager.switchScene("/fxml/viewAnalytics.fxml");
    }

    public void viewAccount() throws IOException {
        SceneManager.switchScene("/fxml/accountSettings");
    }
}
