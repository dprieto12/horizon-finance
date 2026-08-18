package controllers;

import database.DatabaseManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import models.Transaction;
import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;

public class TransactionOptionsController {
    @FXML
    private ListView<Transaction> transactionListView;


    // TODO: Learn ListView to display and handle transactions
    @FXML
    public void initialize() {
        // Grab all transactions for the current account and place them in the ListView
        // NOTE: This could be done in one statement, but it is more readable this way
        int currentAccountID = ApplicationState.getCurrentAccount().getAccountID();
        ObservableList<Transaction> transactionsList = DatabaseManager.getInstance().getTransactions(currentAccountID);
        transactionListView.setItems(transactionsList);

    }



    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }
}
