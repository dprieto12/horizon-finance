package controllers;

import database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import models.Account;
import models.Transaction;
import utils.ApplicationState;
import utils.SceneManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;

public class TransactionOptionsController {
    @FXML
    private ListView<Transaction> transactionListView;

    @FXML
    private TextField amountTextField;

    @FXML
    private RadioButton expenseRadioButton;

    @FXML
    private RadioButton incomeRadioButton;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private ComboBox<String> viewModeComboBox;

    private ObservableList<Transaction> transactionsList;

    @FXML
    public void initialize() {
        SceneManager.setTitle("Transaction Options");

        // Initialize view mode combo box
        viewModeComboBox.setItems(FXCollections.observableArrayList("Recently Added", "By Date"));
        viewModeComboBox.setValue("Recently Added");

        // Initialize category combo box
        categoryComboBox.setItems(FXCollections.observableArrayList(Transaction.purchaseCategories));

        // Restrict date picker to today or earlier
        restrictDatePickerToToday();

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Initialize with expense types selected by default
        // (Radio button listeners won't fire on initialization since buttons are already selected)
        typeComboBox.setItems(FXCollections.observableArrayList(Transaction.expenseTypes));

        // Set up radio button listeners to filter type combo box
        expenseRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                typeComboBox.setItems(FXCollections.observableArrayList(Transaction.expenseTypes));
                typeComboBox.setValue(null);
                categoryComboBox.setDisable(true);
                categoryComboBox.setValue(null);
            }
        });

        incomeRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                typeComboBox.setItems(FXCollections.observableArrayList(Transaction.incomeTypes));
                typeComboBox.setValue(null);
                categoryComboBox.setDisable(true);
                categoryComboBox.setValue(null);
            }
        });

        // Set up type combo box listener to enable/disable category
        typeComboBox.setOnAction(event -> {
            String selectedType = typeComboBox.getValue();
            if (Transaction.requiresCategory(selectedType)) {
                categoryComboBox.setDisable(false);
            } else {
                categoryComboBox.setDisable(true);
                categoryComboBox.setValue(null);
            }
        });

        // Set up view mode combo box listener to reload transactions
        viewModeComboBox.setOnAction(event -> loadTransactions());

        // Load transactions
        loadTransactions();

        // Set custom cell factory to display running balance
        transactionListView.setCellFactory(new Callback<ListView<Transaction>, ListCell<Transaction>>() {
            @Override
            public ListCell<Transaction> call(ListView<Transaction> param) {
                return new ListCell<Transaction>() {
                    @Override
                    protected void updateItem(Transaction transaction, boolean empty) {
                        super.updateItem(transaction, empty);
                        if (empty || transaction == null) {
                            setText(null);
                        } else {
                            // Calculate running balance up to this transaction
                            double runningBalance = calculateRunningBalance(transaction);
                            String display = String.format("$%.2f | %s", transaction.getAmount(), transaction.getType());
                            if (Transaction.requiresCategory(transaction.getType()) && transaction.getCategory() != null) {
                                display += " - " + transaction.getCategory();
                            }
                            display += " | " + transaction.getDate() + " | Balance: $" + String.format("%.2f", runningBalance);
                            setText(display);
                        }
                    }
                };
            }
        });
    }

    /**
     * Restricts the DatePicker to only allow selecting dates up to today.
     * Future dates will be grayed out and unselectable.
     */
    private void restrictDatePickerToToday() {
        LocalDate today = LocalDate.now();
        datePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(today));
            }
        });
    }

    private void loadTransactions() {
        int currentAccountID = ApplicationState.getCurrentAccount().getAccountID();
        transactionsList = DatabaseManager.getInstance().getTransactions(currentAccountID);

        // Sort based on view mode
        String viewMode = viewModeComboBox.getValue();
        if ("By Date".equals(viewMode)) {
            transactionsList.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        }
        // "Recently Added" uses default order from database (likely by transaction ID descending)

        transactionListView.setItems(transactionsList);
    }

    private double calculateRunningBalance(Transaction targetTransaction) {
        Account currentAccount = DatabaseManager.getInstance().getAccount(ApplicationState.getCurrentAccount().getAccountID());
        double balance = currentAccount.getBalance();

        // Start from the end and work backwards to find the balance after this transaction
        // This is more efficient than calculating from the beginning
        boolean foundTarget = false;
        double runningBalance = balance;

        for (int i = transactionsList.size() - 1; i >= 0; i--) {
            Transaction t = transactionsList.get(i);
            if (foundTarget) {
                // Reverse the effect of this transaction to get the balance before it
                if (Transaction.isExpense(t.getType())) {
                    runningBalance += t.getAmount();
                } else {
                    runningBalance -= t.getAmount();
                }
            } else if (t.getTransactionID() == targetTransaction.getTransactionID()) {
                foundTarget = true;
            }
        }

        return runningBalance;
    }

    @FXML
    public void addTransaction() {
        try {
            String amountText = amountTextField.getText().trim();
            if (amountText.isEmpty()) {
                showError("Amount is required");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showError("Amount must be positive");
                return;
            }

            String type = typeComboBox.getValue();
            if (type == null) {
                showError("Type is required");
                return;
            }

            String category = null;
            if (Transaction.requiresCategory(type)) {
                category = categoryComboBox.getValue();
                if (category == null) {
                    showError("Category is required for purchases");
                    return;
                }
            }

            LocalDate date = datePicker.getValue();
            if (date == null || date.isAfter(LocalDate.now())) {
                showError("Date is empty or invalid");
                return;
            }

            int accountID = ApplicationState.getCurrentAccount().getAccountID();
            DatabaseManager.getInstance().createTransaction(accountID, amount, type, category, date);

            // Refresh transactions and account
            loadTransactions();
            ApplicationState.setCurrentAccount(DatabaseManager.getInstance().getAccount(accountID));

            // Clear input fields and reset UI state
            amountTextField.clear();
            typeComboBox.setValue(null);
            categoryComboBox.setValue(null);
            categoryComboBox.setDisable(true);
            datePicker.setValue(LocalDate.now());
            hideError();

        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        }
    }

    @FXML
    public void deleteTransaction() {
        Transaction selectedTransaction = transactionListView.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showError("Please select a transaction to delete");
            return;
        }

        DatabaseManager.getInstance().deleteTransaction(selectedTransaction);

        // Refresh transactions and account
        int accountID = ApplicationState.getCurrentAccount().getAccountID();
        loadTransactions();
        ApplicationState.setCurrentAccount(DatabaseManager.getInstance().getAccount(accountID));

        hideError();
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }

    private void hideError() {
        errorMessageLabel.setVisible(false);
    }

    public void goBack() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }
}
