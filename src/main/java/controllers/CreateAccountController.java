package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import utils.TextFieldFilters;
import utils.SceneManager;
import database.DatabaseManager;

import java.io.IOException;

/**
 * Controller for the createAccount.fxml scene. Handles the creation of a new account, redirecting to the dashboard
 * after creation.
 */

// TODO: Stylize

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

    private TextField[] textFields;

    @FXML
    public void initialize() {
        textFields = new TextField[]{firstNameTextField, lastNameTextField, accountNameTextField,
            dollarTextField, centTextField};

        // Set text field limiters
        firstNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(50));
        lastNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(50));
        accountNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(30));
        dollarTextField.setTextFormatter(TextFieldFilters.createNumberLimitFormatter(9));
        centTextField.setTextFormatter(TextFieldFilters.createNumberLimitFormatter(2));
    }

    public void createNewAccount(ActionEvent event) throws IOException {
        if (fieldsAreFilled()) {
            String firstName = firstNameTextField.getText().trim();
            String lastName = lastNameTextField.getText().trim();
            String accountName = accountNameTextField.getText().trim();
            int dollar = Integer.parseInt(dollarTextField.getText().trim());
            int cent = Integer.parseInt(centTextField.getText().trim());
            double balance = dollar + (cent / 100.0);
            DatabaseManager.getInstance().createNewAccount(accountName, firstName, lastName, balance);
            SceneManager.switchScene("/fxml/chooseAccount.fxml");
        } else {
            errorMessageLabel.setVisible(true);
        }
    }

    private boolean fieldsAreFilled() {
        for (TextField t : textFields) {
            if (t.getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
