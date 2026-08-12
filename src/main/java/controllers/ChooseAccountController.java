package controllers;

import database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import models.Account;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for the ChooseAccount.fxml scene. Displays a list of account buttons and a button to create a new
 * account, prompting the user to select one in order to navigate to the dashboard.
 */

public class ChooseAccountController implements Initializable {
    @FXML
    private VBox accountContainer;

    @FXML
    private Button createAccountButton;

    /**
     * Initializes the controller and sets up the scene, loading accounts from the database and adding them to the
     * accountContainer VBox.
     * @param url
     * @param resourceBundle
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArrayList<Account> accounts = DatabaseManager.getInstance().getAccountList();
        for (Account a : accounts) {
            Button btn = new Button(a.getAccountName() + " - " + a.getFirstName() + " " + a.getLastName() + " - $"
                    + String.format("%2f", a.getBalance()));
            btn.setOnAction(e -> chooseAccount(a));
            accountContainer.getChildren().add(btn);
        }
    }

    private void chooseAccount(Account a) {

    }

    private void createAccount() {

    }
}
