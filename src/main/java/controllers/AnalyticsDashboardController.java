package controllers;

import database.DatabaseManager;
import javafx.collections.ObservableList;
import models.Account;
import models.AccountSummary;
import models.Transaction;
import utils.ApplicationState;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.SceneManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

// TODO: Implement custom dates
// TODO: Implement income/expense bar graphs
// TODO: Implement type/category pie charts
// TODO: Implement top transactions

public class AnalyticsDashboardController {
    @FXML
    private Label accountInfoLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label totalsLabel;

    private Account currentAccount = ApplicationState.getCurrentAccount();

    private ObservableList<Transaction> chosenTransactions;

    @FXML
    public void initialize() throws SQLException {
        updateAccountInfoLabel();
        getAllTime();
    }

    private void updateAccountInfoLabel() {
        accountInfoLabel.setText(currentAccount.getAccountName() + " - " + currentAccount.getFirstName() + " " +
                currentAccount.getLastName());
    }

    public void backToDashboard() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }

    // These methods correspond to buttons and set the chosenTransactions ObservableList, also changing the summaryLabel
    // to the corresponding time period

    public void getThisMonth() throws SQLException {
        summaryLabel.setText("Summary - This Month:");
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                thisMonth);

        setTotalsLabel(thisMonth);
    }

    public void getLast3Months() throws SQLException {
        summaryLabel.setText("Summary - Last 3 Months:");
        LocalDate threeMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(3);
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                threeMonthsAgo);

        setTotalsLabel(threeMonthsAgo);
    }

    public void getLast6Months() throws SQLException {
        summaryLabel.setText("Summary - Last 6 Months:");
        LocalDate sixMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(6);
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                sixMonthsAgo);

        setTotalsLabel(sixMonthsAgo);
    }

    public void getThisYear() throws SQLException {
        summaryLabel.setText("Summary - This Year:");
        LocalDate thisYear = LocalDate.now().withDayOfYear(1);
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                thisYear);

        setTotalsLabel(thisYear);
    }

    public void getAllTime() throws SQLException {
        summaryLabel.setText("Summary - All Time:");
        chosenTransactions = DatabaseManager.getInstance().getTransactions(currentAccount.getAccountID());

        setTotalsLabel(LocalDate.MIN);
    }

    private void setTotalsLabel(LocalDate sinceDate) throws SQLException {
        AccountSummary currentAccountSummary = DatabaseManager.getInstance().getSummary(currentAccount.getAccountID(),
                sinceDate);
        totalsLabel.setText(currentAccountSummary.toString());
    }
}
