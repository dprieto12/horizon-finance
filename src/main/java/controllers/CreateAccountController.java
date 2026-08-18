package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

    private TextField[] textFields;

    @FXML
    public void initialize() {
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
            DatabaseManager.getInstance().createNewAccount(accountName, firstName, lastName, balance);
            SceneManager.switchScene("/fxml/chooseAccount.fxml");
        } else {
            errorMessageLabel.setVisible(true);
        }
    }
}
