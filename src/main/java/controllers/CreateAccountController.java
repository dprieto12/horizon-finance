package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import utils.TextFieldFilters;
import utils.SceneManager;

import java.io.IOException;

/**
 * Controller for the createAccount.fxml scene. Handles the creation of a new account, redirecting to the dashboard
 * after creation.
 */

// TODO: Stylize

// TODO: Implement reception of text field data & creation of an account when the create button is clicked
// TODO: Implement a character / length limiter for the text fields
// TODO: Implement validation for the text fields & show message when not all fields are filled

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

    private final TextField[] textFields = {firstNameTextField, lastNameTextField, accountNameTextField,
            dollarTextField, centTextField};

    @FXML
    public void initialize() {
        // Set text field limiters
        firstNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(50));
        lastNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(50));
        accountNameTextField.setTextFormatter(TextFieldFilters.createLengthLimitFormatter(30));
        dollarTextField.setTextFormatter(TextFieldFilters.createNumberLimitFormatter(9));
        centTextField.setTextFormatter(TextFieldFilters.createNumberLimitFormatter(2));
    }

    public void createNewAccount() throws IOException {
    }

    private boolean fieldsAreFilled() {

    }
}
