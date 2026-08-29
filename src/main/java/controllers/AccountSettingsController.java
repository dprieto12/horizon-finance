package controllers;

import database.DatabaseManager;
import javafx.scene.control.*;
import models.Account;
import utils.ApplicationState;
import utils.SceneManager;
import utils.TextFieldUtils;

import javafx.fxml.FXML;

import java.io.IOException;

// TODO: Document
// TODO: Stylize

public class AccountSettingsController {
    @FXML
    private Label currentBalanceLabel;

    @FXML
    private Label fieldsMissingLabel;

    @FXML
    private TextField accountNameTextField;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    private TextField[] textFields;

    @FXML
    public void initialize() {
        SceneManager.setTitle("Account Settings");

        updateBalanceLabel();
        setTextFields();
    }

    private void updateBalanceLabel() {
        String customBalance = "Account Balance: $" +
                String.format("%.2f", ApplicationState.getCurrentAccount().getBalance());
        currentBalanceLabel.setText(customBalance);
    }

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

    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }

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

    public void deleteAccount() throws IOException {
        // Create an alert for the user when they want to delete their account
        Alert accountDeletionAlert = new Alert(Alert.AlertType.WARNING);
        accountDeletionAlert.setTitle("Delete Account");
        accountDeletionAlert.setHeaderText("Are you sure you would like to delete your account?\n" +
                ApplicationState.getCurrentAccount().getAccountName() +
                " - " + ApplicationState.getCurrentAccount().getFirstName() +
                " " + ApplicationState.getCurrentAccount().getLastName());
        accountDeletionAlert.setContentText("This action cannot be undone.");
        accountDeletionAlert.showAndWait();

        // If the user confirms, delete the account from the database and set the current account to null
        DatabaseManager.getInstance().deleteAccount(ApplicationState.getCurrentAccount());
        ApplicationState.setCurrentAccount(null);

        // Switch to the choose account scene
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
    }
}
