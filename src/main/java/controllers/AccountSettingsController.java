package controllers;

import database.DatabaseManager;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.Account;
import utils.ApplicationState;
import utils.SceneManager;
import utils.TextFieldUtils;

import javafx.fxml.FXML;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AccountSettingsController {
    // Dynamic labels
    @FXML
    private Label currentBalanceLabel;

    @FXML
    private Label fieldsMissingLabel;

    // TextFields (must be made dynamic to set limiters)
    @FXML
    private TextField accountNameTextField;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    // For fieldsAreFilled() method
    private TextField[] textFields;

    /**
     * Initializes the controller by setting the title, updating the balance label, and setting the text fields.
     */
    @FXML
    public void initialize() {
        SceneManager.setTitle("Account Settings");

        updateBalanceLabel();
        setTextFields();
    }

    /**
     * Updates the balance label with the current account's balance.
     */
    private void updateBalanceLabel() {
        String customBalance = "Account Balance: $" +
                String.format("%.2f", ApplicationState.getCurrentAccount().getBalance());
        currentBalanceLabel.setText(customBalance);
    }

    /**
     * Sets the text fields to the current account's values and sets the limiters. Additionally, it initializes the
     * textFields array for when fieldsAreFilled() is called later.
     */
    private void setTextFields() {
        // Create TextField array for fieldsAreFilled() method
        textFields = new TextField[]{accountNameTextField, firstNameTextField, lastNameTextField};

        // Set TextField values to current account values
        accountNameTextField.setText(ApplicationState.getCurrentAccount().getAccountName());
        firstNameTextField.setText(ApplicationState.getCurrentAccount().getFirstName());
        lastNameTextField.setText(ApplicationState.getCurrentAccount().getLastName());

        // Set TextField limiters
        accountNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(30));
        firstNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(50));
        lastNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(50));
    }

    /**
     * Navigates the user back to the dashboard.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }

    /**
     * Grabs new account data from TextFields and updates the database, then taking the user back to the dashboard. If
     * the TextFields are not filled, a red label is shown.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void updateAccount() throws IOException {
        // Check to make sure all fields are filled
        if (TextFieldUtils.fieldsAreFilled(textFields)) {
            // Grab the currently selected account
            Account currentAccount = ApplicationState.getCurrentAccount();

            // Update the Account object fields
            currentAccount.setAccountName(accountNameTextField.getText().trim());
            currentAccount.setFirstName(firstNameTextField.getText().trim());
            currentAccount.setLastName(lastNameTextField.getText().trim());

            // Update the database to match the Account object
            DatabaseManager.getInstance().updateAccount(currentAccount);

            // Switch to the dashboard
            SceneManager.switchScene("/fxml/dashboard.fxml");
        } else {
            // If not, show red label
            fieldsMissingLabel.setVisible(true);
        }
    }

    /**
     * Displays an alert to the user when they want to delete their account and if confirmed, deletes the account from
     * the database and takes the user back to the account selection screen.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void deleteAccount() throws IOException {
        // Create an alert for the user when they want to delete their account
        Alert accountDeletionAlert = new Alert(Alert.AlertType.WARNING, "Are you sure?", ButtonType.YES, ButtonType.NO);
        accountDeletionAlert.setTitle("Delete Account");
        accountDeletionAlert.setHeaderText("Are you sure you would like to delete your account?\n" +
                ApplicationState.getCurrentAccount().getAccountName() +
                " - " + ApplicationState.getCurrentAccount().getFirstName() +
                " " + ApplicationState.getCurrentAccount().getLastName());
        accountDeletionAlert.setContentText("This action cannot be undone.");

        // Apply dark title bar to the alert dialog
        accountDeletionAlert.initOwner(SceneManager.getStage());
        accountDeletionAlert.getDialogPane().getScene().getWindow().showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                SceneManager.setDarkTitleBar(accountDeletionAlert.getDialogPane().getScene().getWindow());
            }
        });

        // Show the alert and wait for user response
        accountDeletionAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // If the user confirms, delete the account from the database and set the current account to null
                DatabaseManager.getInstance().deleteAccount(ApplicationState.getCurrentAccount());
                ApplicationState.setCurrentAccount(null);

                try {
                    // Switch to the choose account scene
                    SceneManager.switchScene("/fxml/chooseAccount.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // If the user clicks NO or closes the alert, they remain on the account settings page
        });
    }

    /**
     * Opens the GitHub page for the application in the default browser when the hyperlink is clicked.
     */
    public void openGitHubPage() {
        String url = "https://github.com/dprieto12/horizon-finance";

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
