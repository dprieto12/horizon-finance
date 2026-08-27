package controllers;

import database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Document
// TODO: Stylize

// TODO: Implement custom dates
// TODO: Implement type/category pie charts
// TODO: Implement top transactions

public class AnalyticsDashboardController {
    @FXML
    private VBox mainVBox;

    @FXML
    private Label accountInfoLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label invalidDateLabel;

    @FXML
    private Label totalsLabel;

    private Account currentAccount = ApplicationState.getCurrentAccount();

    private ObservableList<Transaction> chosenTransactions;

    private StackedBarChart<Number, String> barChart;

    private Label topTransactionsLabel;

    @FXML
    public void initialize() {
        updateAccountInfoLabel();
        try {
            getAllTime();
        } catch (SQLException e) {
            e.printStackTrace();
            totalsLabel.setText("Error loading data");
        }
    }

    private void updateAccountInfoLabel() {
        accountInfoLabel.setText(currentAccount.getAccountName() + " - " + currentAccount.getFirstName() + " " +
                currentAccount.getLastName());
    }

    public void backToDashboard() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }

    // TODO: Consolodate outer helper methods into a refresh() method to avoid code duplication

    // These methods correspond to buttons and set the chosenTransactions ObservableList, also changing the summaryLabel
    // to the corresponding time period

    public void getThisMonth() throws SQLException {
        summaryLabel.setText("Summary - This Month:");
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                thisMonth, today);

        setTotalsLabel(thisMonth, today);
        plotIncomeExpenseBarChart();
        setTopTransactionsLabel();
    }

    public void getLast3Months() throws SQLException {
        summaryLabel.setText("Summary - Last 3 Months:");
        LocalDate threeMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(3);
        LocalDate today = LocalDate.now();
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                threeMonthsAgo, today);

        setTotalsLabel(threeMonthsAgo, today);
        plotIncomeExpenseBarChart();
        setTopTransactionsLabel();
    }

    public void getLast6Months() throws SQLException {
        summaryLabel.setText("Summary - Last 6 Months:");
        LocalDate sixMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(6);
        LocalDate today = LocalDate.now();
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                sixMonthsAgo, today);

        setTotalsLabel(sixMonthsAgo, today);
        plotIncomeExpenseBarChart();
        setTopTransactionsLabel();
    }

    public void getThisYear() throws SQLException {
        summaryLabel.setText("Summary - This Year:");
        LocalDate thisYear = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();
        chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                thisYear, today);

        setTotalsLabel(thisYear, today);
        plotIncomeExpenseBarChart();
        setTopTransactionsLabel();
    }

    public void getAllTime() throws SQLException {
        summaryLabel.setText("Summary - All Time:");
        chosenTransactions = DatabaseManager.getInstance().getTransactions(currentAccount.getAccountID());

        setTotalsLabel(LocalDate.MIN, LocalDate.now());
        plotIncomeExpenseBarChart();
        setTopTransactionsLabel();
    }

    public void customDate() throws SQLException {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        boolean invalidDate = fromDate == null || toDate == null || fromDate.isAfter(toDate) || toDate.isBefore(LocalDate.now());

        if (invalidDate) {
            invalidDateLabel.setVisible(true);
        } else {
            invalidDateLabel.setVisible(false);
            summaryLabel.setText("Summary - Custom Range:");
            chosenTransactions = DatabaseManager.getInstance().getTransactionsByDateRange(currentAccount.getAccountID(),
                    fromDate, toDate);

            setTotalsLabel(fromDate, toDate);
            plotIncomeExpenseBarChart();
            setTopTransactionsLabel();
        }
    }

    private void setTotalsLabel(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        AccountSummary currentAccountSummary = DatabaseManager.getInstance().getSummary(currentAccount.getAccountID(),
                sinceDate, toDate);
        totalsLabel.setText(currentAccountSummary.toString());
    }
    
    private void plotIncomeExpenseBarChart() {
        removeExistingChart();

        Map<String, Double> incomeValues = getMapByTypeArray(Transaction.incomeTypes);
        Map<String, Double> expenseValues = getMapByTypeArray(Transaction.expenseTypes);

        Set<String> allMonths = getAllMonths(incomeValues, expenseValues);

        barChart = createChart();

        XYChart.Series<Number, String> incomeSeries = createSeriesFromMap(incomeValues, allMonths, "Income");
        XYChart.Series<Number, String> expenseSeries = createSeriesFromMap(expenseValues, allMonths, "Expenses");

        barChart.getData().addAll(incomeSeries, expenseSeries);
        styleBars();

        mainVBox.getChildren().add(barChart);
    }

    private void removeExistingChart() {
        if (barChart != null) {
            mainVBox.getChildren().remove(barChart);
        }
    }

    private Set<String> getAllMonths(Map<String, Double> incomeValues, Map<String, Double> expenseValues) {
        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(incomeValues.keySet());
        allMonths.addAll(expenseValues.keySet());
        return allMonths;
    }

    private StackedBarChart<Number, String> createChart() {
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Month");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Amount");
        xAxis.setTickUnit(100);
        xAxis.setMinorTickVisible(false);

        StackedBarChart<Number, String> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Income vs. Expenses");
        chart.setLegendVisible(true);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        return chart;
    }

    private XYChart.Series<Number, String> createSeriesFromMap(Map<String, Double> values, Set<String> allMonths, String seriesName) {
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (String month : allMonths) {
            double amount = values.getOrDefault(month, 0.0);
            XYChart.Data<Number, String> data = new XYChart.Data<>(amount, month);
            series.getData().add(data);
        }

        return series;
    }

    private void styleBars() {
        for (XYChart.Series<Number, String> series : barChart.getData()) {
            for (XYChart.Data<Number, String> data : series.getData()) {
                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    applyBarColor(node, series.getName());
                    addTooltip(node, series.getName(), data.getXValue());
                }
            }
        }
    }

    private void applyBarColor(javafx.scene.Node node, String seriesName) {
        if (seriesName.equals("Income")) {
            node.setStyle("-fx-bar-fill: #4CAF50;"); // Green
        } else {
            node.setStyle("-fx-bar-fill: #F44336;"); // Red
        }
    }

    private void addTooltip(javafx.scene.Node node, String seriesName, Number value) {
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                seriesName + ": $" + String.format("%.2f", value));
        javafx.scene.control.Tooltip.install(node, tooltip);
    }

    private Map<String, Double> getMapByTypeArray(String[] types) {
        Map<String, Double> monthlyValues = chosenTransactions.stream()
                .filter(t -> Arrays.asList(types).contains(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        return monthlyValues;
    }

    // TODO: implement display of pie charts
    private void plotPieCharts() {

    }

    // TODO: Implement display of top transactions from chosen time period
    private void setTopTransactionsLabel() {
        removeExistingTopTransactionsLabel();

        topTransactionsLabel = new Label();
        topTransactionsLabel.setFont(new Font(14));

        ObservableList<Transaction> topFiveList = getTopTransactions();
        if (topFiveList.isEmpty()) {
            topTransactionsLabel.setText("Top Transactions:\nNo transactions found for this period.");
        } else {
            StringBuilder sb = new StringBuilder("Top Transactions:\n");
            for (Transaction transaction : topFiveList) {
                sb.append(transaction.toString()).append("\n");
            }
            topTransactionsLabel.setText(sb.toString());
        }

        mainVBox.getChildren().add(topTransactionsLabel);
    }

    private void removeExistingTopTransactionsLabel() {
        if (topTransactionsLabel != null) {
            mainVBox.getChildren().remove(topTransactionsLabel);
        }
    }

    private ObservableList<Transaction> getTopTransactions() {
        ObservableList<Transaction> topFiveList = chosenTransactions.stream()
                .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                .limit(5)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        return topFiveList;
    }
}
