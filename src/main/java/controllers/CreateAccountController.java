package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import models.Account;
import utils.TextFieldUtils;
import utils.SceneManager;
import database.DatabaseManager;

import java.io.IOException;

/**
 * Controller for the createAccount.fxml scene. Handles the creation of a new account, redirecting to the dashboard
 * after creation.
 */

public class CreateAccountController {
    // TextFields in the scene (must be tagged in order to set limiters)

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField accountNameTextField;

    @FXML
    private TextField dollarTextField;

    @FXML
    private TextField centTextField;

    // Holds all TextFields so they can be easily iterated over
    private TextField[] textFields;

    // All other dynamic components in the scene
    @FXML
    private Label errorMessageLabel;

    @FXML
    private ToggleGroup colorToggleGroup;

    /**
     * Initializes the controller by setting the title and setting up text field limiters.
     */
    @FXML
    public void initialize() {
        SceneManager.setTitle("Create Account");

        textFields = new TextField[]{firstNameTextField, lastNameTextField, accountNameTextField,
            dollarTextField, centTextField};

        // Set text field limiters
        firstNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(50));
        lastNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(50));
        accountNameTextField.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(30));
        dollarTextField.setTextFormatter(TextFieldUtils.createNumberLimitFormatter(9));
        centTextField.setTextFormatter(TextFieldUtils.createNumberLimitFormatter(2));
    }

    /**
     * Creates a new account with the provided information in the TextFields and redirects the user to the dashboard.
     * If the TextFields are not filled, the error message is displayed.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void createNewAccount() throws IOException {
        if (TextFieldUtils.fieldsAreFilled(textFields)) {
            // Get text from text fields and parse balance
            String firstName = firstNameTextField.getText().trim();
            String lastName = lastNameTextField.getText().trim();
            String accountName = accountNameTextField.getText().trim();
            int dollar = Integer.parseInt(dollarTextField.getText().trim());
            int cent = Integer.parseInt(centTextField.getText().trim());
            double balance = dollar + (cent / 100.0);

            // Create a new account and save it to the database
            DatabaseManager.getInstance().createNewAccount(accountName, firstName, lastName, balance,
                    getSelectedColor());

            // Switch back to the account selection screen
            goBack();
        } else {
            errorMessageLabel.setVisible(true);
        }
    }

    /**
     * Reads the palette index from the selected color swatch, which each swatch carries as its userData. Falls back to
     * the default color if nothing is selected, so an account is never created without one.
     * @return Palette index from 1 to Account.COLOR_COUNT
     */
    private int getSelectedColor() {
        Toggle selectedSwatch = colorToggleGroup.getSelectedToggle();
        if (selectedSwatch == null || selectedSwatch.getUserData() == null) {
            return Account.DEFAULT_COLOR;
        }

        try {
            return Integer.parseInt(selectedSwatch.getUserData().toString());
        } catch (NumberFormatException e) {
            System.err.println("Unreadable color swatch value: " + selectedSwatch.getUserData());
            return Account.DEFAULT_COLOR;
        }
    }

    /**
     * Takes the user back to the account selection screen, bound to the back button and called when creating a new
     * account.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
    }
}
