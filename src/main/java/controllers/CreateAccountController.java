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

// TODO: Stylize
// TODO: Document

public class CreateAccountController {
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

    @FXML
    private Label errorMessageLabel;

    @FXML
    private ToggleGroup colorToggleGroup;

    private TextField[] textFields;

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

    public void createNewAccount(ActionEvent event) throws IOException {
        if (TextFieldUtils.fieldsAreFilled(textFields)) {
            String firstName = firstNameTextField.getText().trim();
            String lastName = lastNameTextField.getText().trim();
            String accountName = accountNameTextField.getText().trim();
            int dollar = Integer.parseInt(dollarTextField.getText().trim());
            int cent = Integer.parseInt(centTextField.getText().trim());
            double balance = dollar + (cent / 100.0);
            DatabaseManager.getInstance().createNewAccount(accountName, firstName, lastName, balance,
                    getSelectedColor());
            SceneManager.switchScene("/fxml/chooseAccount.fxml");
        } else {
            errorMessageLabel.setVisible(true);
        }
    }

    /**
     * Reads the palette index from the selected color swatch, which each swatch carries as its userData.
     * Falls back to the default color if nothing is selected, so an account is never created without one.
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

    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/chooseAccount.fxml");
    }
}
