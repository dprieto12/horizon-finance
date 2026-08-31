package controllers;

import database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
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
        SceneManager.setTitle("Choose Account");

        try {
            ArrayList<Account> accounts = DatabaseManager.getInstance().getAccountList();
            for (Account a : accounts) {
                accountContainer.getChildren().add(createAccountTile(a));
            }
        } catch (Exception e) {
            System.err.println("Error initializing ChooseAccountController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds the button for a single account. The account's stored palette index selects an .account-color-N
     * class, which styles both the tile's outline and the dot carried as its graphic, so the color lives in
     * styles.css and follows the theme rather than being set here.
     * @param account Account the tile represents
     * @return Button that selects the account when pressed
     */
    private Button createAccountTile(Account account) {
        Button tile = new Button(account.getAccountName() + " - " + account.getFirstName() + " "
                + account.getLastName() + " - $" + String.format("%.2f", account.getBalance()));

        Region colorDot = new Region();
        colorDot.getStyleClass().add("account-dot");
        tile.setGraphic(colorDot);

        tile.getStyleClass().addAll("account-tile", "account-color-" + account.getColor());
        tile.setOnAction(e -> chooseAccount(account));
        return tile;
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

    /** Bound to the create button in chooseAccount.fxml, which sits outside the scrolling account list. */
    @FXML
    private void createAccount() {
        try {
            SceneManager.switchScene("/fxml/createAccount.fxml");
        } catch (IOException e) {
            System.err.println("Error switching to create account scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
