package controllers;

import database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import models.Account;
import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;
import java.util.ArrayList;

// TODO: Stylize & set a limit for how many accounts can be made

/**
 * Controller for the ChooseAccount.fxml scene. Displays a list of account buttons and a button to create a new
 * account, prompting the user to select one in order to navigate to the dashboard.
 */

public class ChooseAccountController {
    @FXML
    private VBox accountContainer;

    /**
     * Initializes the controller and sets up the scene, loading accounts from the database and adding them to the
     * accountContainer VBox.
     */

    @FXML
    public void initialize() {
        try {
            ArrayList<Account> accounts = DatabaseManager.getInstance().getAccountList();
            for (Account a : accounts) {
                Button btn = new Button(a.getAccountName() + " - " + a.getFirstName() + " " + a.getLastName() + " - $"
                        + String.format("%.2f", a.getBalance()));
                btn.setOnAction(e -> chooseAccount(a));
                accountContainer.getChildren().add(btn);
            }
            Button createAccountButton = new Button("Create new account");
            createAccountButton.setOnAction(e -> createAccount());
            accountContainer.getChildren().add(createAccountButton);
        } catch (Exception e) {
            System.err.println("Error initializing ChooseAccountController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chooseAccount(Account a) {
        try {
            ApplicationState.setCurrentAccount(a);
            SceneManager.switchScene("/fxml/dashboard.fxml");
        } catch (IOException e) {
            System.err.println("Error switching to dashboard scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAccount() {
        try {
            SceneManager.switchScene("/fxml/createAccount.fxml");
        } catch (IOException e) {
            System.err.println("Error switching to create account scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
