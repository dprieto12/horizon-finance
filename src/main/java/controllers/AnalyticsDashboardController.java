package controllers;

import database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
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

    private StackedBarChart<Number, String> barChart;

    private AreaChart<String, Number> balanceLineChart;

    private Label topTransactionsLabel;

    @FXML
    public void initialize() {
        SceneManager.setTitle("Analytics");

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

    // These methods correspond to buttons, changing data based on the time period

    public void getThisMonth() throws SQLException {
        summaryLabel.setText("Summary - This Month:");
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        refreshPage(thisMonth, today);
    }

    public void getLast3Months() throws SQLException {
        summaryLabel.setText("Summary - Last 3 Months:");
        LocalDate threeMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(3);
        LocalDate today = LocalDate.now();

        refreshPage(threeMonthsAgo, today);
    }

    public void getLast6Months() throws SQLException {
        summaryLabel.setText("Summary - Last 6 Months:");
        LocalDate sixMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(6);
        LocalDate today = LocalDate.now();

        refreshPage(sixMonthsAgo, today);
    }

    public void getThisYear() throws SQLException {
        summaryLabel.setText("Summary - This Year:");
        LocalDate thisYear = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();

        refreshPage(thisYear, today);
    }

    public void getAllTime() throws SQLException {
        summaryLabel.setText("Summary - All Time:");

        refreshPage(LocalDate.MIN, LocalDate.now());
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

            setTotalsLabel(fromDate, toDate);
            plotIncomeExpenseBarChart(fromDate, toDate);
            setTopTransactionsLabel(fromDate, toDate);
        }
    }

    private void setTotalsLabel(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        AccountSummary currentAccountSummary = DatabaseManager.getInstance().getSummary(currentAccount.getAccountID(),
                sinceDate, toDate);
        totalsLabel.setText(currentAccountSummary.toString());
    }
    
    private void plotIncomeExpenseBarChart(LocalDate sinceDate, LocalDate toDate) {
        removeExistingChart();

        Map<String, Map<String, Double>> monthlyData = DatabaseManager.getInstance().getMonthlyIncomeExpenseByDateRange(
                currentAccount.getAccountID(), sinceDate, toDate);

        Map<String, Double> incomeValues = monthlyData.get("Income");
        Map<String, Double> expenseValues = monthlyData.get("Expense");

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
        if (balanceLineChart != null) {
            mainVBox.getChildren().remove(balanceLineChart);
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

    private void plotBalanceLineChart(LocalDate sinceDate, LocalDate toDate) {
        removeExistingBalanceChart();

        Map<LocalDate, Double> fullBalanceHistory = DatabaseManager.getInstance()
                .getBalanceHistory(currentAccount.getAccountID());

        // Filter to date range and convert to sorted list
        List<Map.Entry<LocalDate, Double>> filteredEntries = fullBalanceHistory.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(LocalDate.MIN))
                .filter(entry -> (entry.getKey().isEqual(sinceDate) || entry.getKey().isAfter(sinceDate)) &&
                        (entry.getKey().isEqual(toDate) || entry.getKey().isBefore(toDate)))
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        if (filteredEntries.isEmpty()) {
            Label noDataLabel = new Label("No balance data available for this period");
            noDataLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            mainVBox.getChildren().add(noDataLabel);
            return;
        }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Balance ($)");

        balanceLineChart = new AreaChart<>(xAxis, yAxis);
        balanceLineChart.setTitle("Account Balance Over Time");
        balanceLineChart.setLegendVisible(false);
        balanceLineChart.setCreateSymbols(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Double> entry : filteredEntries) {
            String dateStr = entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            series.getData().add(new XYChart.Data<>(dateStr, entry.getValue()));
        }

        balanceLineChart.getData().add(series);
        mainVBox.getChildren().add(balanceLineChart);
    }

    private void removeExistingBalanceChart() {
        if (balanceLineChart != null) {
            mainVBox.getChildren().remove(balanceLineChart);
        }
        mainVBox.getChildren().removeIf(node ->
            node instanceof Label &&
            ((Label) node).getText().equals("No balance data available for this period")
        );
    }


    // TODO: If data is empty, show message
    private void plotPieCharts(LocalDate sinceDate, LocalDate toDate) {
        removeExistingPieCharts();

        Map<String, Double> typeAmountMap = DatabaseManager.getInstance().getTransactionsByTypeAndDateRange(
                currentAccount.getAccountID(),
                sinceDate,
                toDate
        );

        Map<String, Double> purchaseCategoryAmountMap = DatabaseManager.getInstance().getPurchasesByCategoryAndDateRange(
                currentAccount.getAccountID(),
                sinceDate,
                toDate
        );

        plotIncomePieChart(typeAmountMap);
        plotExpensePieChart(typeAmountMap);
        plotPurchasePieChart(purchaseCategoryAmountMap);
    }

    private void plotIncomePieChart(Map<String, Double> typeAmountMap) {
        ObservableList<PieChart.Data> incomePieChartData = FXCollections.observableArrayList();

        typeAmountMap.forEach((type, amount) -> {
            if (!Transaction.isExpense(type)) {
                incomePieChartData.add(new PieChart.Data(pluralizeType(type), amount / typeAmountMap.values().stream().mapToDouble(Double::doubleValue).sum()));
            }
        });

        if (incomePieChartData.isEmpty()) {
            Label noDataLabel = new Label("No income data available for this period");
            noDataLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            mainVBox.getChildren().add(noDataLabel);
        } else {
            PieChart incomePieChart = new PieChart(incomePieChartData);
            incomePieChart.setTitle("Income by Type");
            incomePieChart.setLegendVisible(true);
            incomePieChart.setLabelsVisible(true);

            mainVBox.getChildren().add(incomePieChart);
        }
    }

    private void plotExpensePieChart(Map<String, Double> typeAmountMap) {
        ObservableList<PieChart.Data> expensePieChartData = FXCollections.observableArrayList();

        typeAmountMap.forEach((type, amount) -> {
            if (Transaction.isExpense(type)) {
                expensePieChartData.add(new PieChart.Data(pluralizeType(type), amount / typeAmountMap.values().stream().mapToDouble(Double::doubleValue).sum()));
            }
        });

        if (expensePieChartData.isEmpty()) {
            Label noDataLabel = new Label("No expense data available for this period");
            noDataLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            mainVBox.getChildren().add(noDataLabel);
        } else {
            PieChart expensePieChart = new PieChart(expensePieChartData);
            expensePieChart.setTitle("Expenses by Type");
            expensePieChart.setLegendVisible(true);
            expensePieChart.setLabelsVisible(true);

            mainVBox.getChildren().add(expensePieChart);
        }
    }

    private void plotPurchasePieChart(Map<String, Double> purchaseCategoryAmountMap) {
        ObservableList<PieChart.Data> purchasePieChartData = FXCollections.observableArrayList();

        purchaseCategoryAmountMap.forEach((category, amount) -> {
            purchasePieChartData.add(new PieChart.Data(category, amount));
        });

        if (purchasePieChartData.isEmpty()) {
            Label noDataLabel = new Label("No purchase data available for this period");
            noDataLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            mainVBox.getChildren().add(noDataLabel);
        } else {
            PieChart purchasePieChart = new PieChart(purchasePieChartData);
            purchasePieChart.setTitle("Purchases by Category");
            purchasePieChart.setLegendVisible(true);
            purchasePieChart.setLabelsVisible(true);

            mainVBox.getChildren().add(purchasePieChart);
        }
    }

    private void removeExistingPieCharts() {
        mainVBox.getChildren().removeIf(node -> node instanceof PieChart ||
            (node instanceof Label && ((Label) node).getText().contains("No ") && ((Label) node).getText().contains(" data available")));
    }

    private String pluralizeType(String type) {
        if (type == null) return type;
        switch (type) {
            case "Sale": return "Sales";
            case "Purchase": return "Purchases";
            case "Transfer": return "Transfers";
            case "Withdrawal": return "Withdrawals";
            case "Bill": return "Bills";
            case "Fee": return "Fees";
            case "Gift": return "Gifts";
            case "Refund": return "Refunds";
            default: return type; // Wages, Interest already plural or non-countable
        }
    }


    private void setTopTransactionsLabel(LocalDate sinceDate, LocalDate toDate) {
        removeExistingTopTransactionsLabel();

        topTransactionsLabel = new Label();
        topTransactionsLabel.setFont(new Font(14));

        ObservableList<Transaction> topFiveList = DatabaseManager.getInstance().getTopTransactionsByDateRange(
                currentAccount.getAccountID(), sinceDate, toDate, 5);
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

    private void refreshPage(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        setTotalsLabel(sinceDate, toDate);
        plotIncomeExpenseBarChart(sinceDate, toDate);
        plotPieCharts(sinceDate, toDate);
        plotBalanceLineChart(sinceDate, toDate);
        setTopTransactionsLabel(sinceDate, toDate);
    }

}
