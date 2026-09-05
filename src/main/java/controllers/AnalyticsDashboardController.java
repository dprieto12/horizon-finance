package controllers;

import database.DatabaseManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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


public class AnalyticsDashboardController {

    /** Chart height as a fraction of the available width, keeping each chart to a stable aspect ratio. */
    private static final double WIDE_CHART_RATIO = 0.36;
    private static final double PIE_CHART_RATIO = 0.34;
    private static final double PURCHASE_CHART_RATIO = 0.42;

    /** Height floors, so charts stay legible when the window is at its smallest. */
    private static final double WIDE_CHART_MIN_HEIGHT = 280;
    private static final double PIE_CHART_MIN_HEIGHT = 260;
    private static final double PURCHASE_CHART_MIN_HEIGHT = 320;

    /**
     * Width the purchases pie is allowed to take on its own row. A pie's usable size is driven by height, so
     * letting it span the full width would only add empty space either side of the same-sized pie.
     */
    private static final double PURCHASE_CHART_WIDTH_SHARE = 0.6;


    // Containers
    @FXML
    private ScrollPane analyticsScrollPane;

    @FXML
    private VBox mainVBox;

    @FXML
    private VBox barChartBox;

    @FXML
    private HBox pieChartRow;

    @FXML
    private VBox purchaseChartBox;

    @FXML
    private VBox balanceChartBox;

    @FXML
    private VBox topTransactionsList;


    // Labels
    @FXML
    private Label accountInfoLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label invalidDateLabel;

    @FXML
    private Label dataErrorLabel;

    @FXML
    private Label incomeValueLabel;

    @FXML
    private Label expensesValueLabel;

    @FXML
    private Label netValueLabel;

    @FXML
    private Label avgValueLabel;


    // DatePickers
    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;


    // Charts
    private StackedBarChart<Number, String> barChart;

    private AreaChart<String, Number> balanceLineChart;


    // Current Account Instance
    private Account currentAccount;


    /**
     * Initializes the controller by setting the title, updating the account info label, binding responsive spacing,
     * restricting date pickers to today, and loading all time data.
     */
    @FXML
    public void initialize() {
        SceneManager.setTitle("Analytics");

        currentAccount = ApplicationState.getCurrentAccount();

        updateAccountInfoLabel();
        bindResponsiveSpacing();
        restrictDatePickersToToday();
        try {
            getAllTime();
        } catch (SQLException e) {
            e.printStackTrace();
            dataErrorLabel.setText("Error loading data");
            dataErrorLabel.setVisible(true);
        }
    }

    /**
     * Grows the gaps between sections along with the window, so a wide window spreads content out instead of
     * leaving the same tight gutters it needs when narrow.
     */
    private void bindResponsiveSpacing() {
        NumberBinding spacing = Bindings.max(16, analyticsScrollPane.widthProperty().multiply(0.018));
        mainVBox.spacingProperty().bind(spacing);
        pieChartRow.spacingProperty().bind(spacing);
    }

    /**
     * Restricts both DatePickers to only allow selecting dates up to today.
     * Future dates will be grayed out and unselectable.
     */
    private void restrictDatePickersToToday() {
        LocalDate today = LocalDate.now();
        fromDatePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(today));
            }
        });
        toDatePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(today));
            }
        });
    }

    /**
     * Lets a chart fill the width available to it and take its height from that width, so charts scale up as
     * the window is expanded rather than staying at a fixed size.
     * @param chart Chart to size
     * @param heightRatio Height as a fraction of the scroll pane's width
     * @param minHeight Smallest height in pixels, applied when the window is narrow
     */
    private void makeChartResponsive(Chart chart, double heightRatio, double minHeight) {
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.prefHeightProperty().bind(
                Bindings.max(minHeight, analyticsScrollPane.widthProperty().multiply(heightRatio)));
    }

    /** Placeholder shown in place of a chart when the selected period has nothing to plot. */
    private Label createChartMessage(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("chart-message");
        return label;
    }

    private String formatAmount(double amount) {
        return "$" + String.format("%.2f", amount);
    }

    /**
     * Shows a signed figure and colors it by direction, so a negative net reads as an expense without the
     * reader having to parse the sign.
     */
    private void setSignedAmount(Label label, double amount) {
        label.setText((amount < 0 ? "-$" : "+$") + String.format("%.2f", Math.abs(amount)));
        label.getStyleClass().removeAll("amount-income", "amount-expense");
        label.getStyleClass().add(amount < 0 ? "amount-expense" : "amount-income");
    }

    /**
     * Updates the account info label with the current account's name and the user's full name.
     */
    private void updateAccountInfoLabel() {
        accountInfoLabel.setText(currentAccount.getAccountName() + " - " + currentAccount.getFirstName() + " " +
                currentAccount.getLastName());
    }

    /**
     * Navigates the user back to the dashboard scene.
     * @throws IOException If the scene switch fails when calling SceneManager.switchScene(String FXMLPath)
     */
    public void backToDashboard() throws IOException {
        SceneManager.switchScene("/fxml/dashboard.fxml");
    }



    // These methods correspond to buttons, changing data based on the time period

    /**
     * Updates the summary label and refreshes the page to show data for the current month.
     * @throws SQLException If the database query fails
     */
    public void getThisMonth() throws SQLException {
        summaryLabel.setText("Summary - This Month:");
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        refreshPage(thisMonth, today);
    }

    /**
     * Updates the summary label and refreshes the page to show data for the last 3 months.
     * @throws SQLException If the database query fails
     */
    public void getLast3Months() throws SQLException {
        summaryLabel.setText("Summary - Last 3 Months:");
        LocalDate threeMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(3);
        LocalDate today = LocalDate.now();

        refreshPage(threeMonthsAgo, today);
    }

    /**
     * Updates the summary label and refreshes the page to show data for the last 6 months.
     * @throws SQLException If the database query fails
     */
    public void getLast6Months() throws SQLException {
        summaryLabel.setText("Summary - Last 6 Months:");
        LocalDate sixMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(6);
        LocalDate today = LocalDate.now();

        refreshPage(sixMonthsAgo, today);
    }

    /**
     * Updates the summary label and refreshes the page to show data for the last year.
     * @throws SQLException If the database query fails
     */
    public void getThisYear() throws SQLException {
        summaryLabel.setText("Summary - This Year:");
        LocalDate thisYear = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();

        refreshPage(thisYear, today);
    }

    /**
     * Updates the summary label and refreshes the page to show all transactional data.
     * @throws SQLException If the database query fails
     */
    public void getAllTime() throws SQLException {
        summaryLabel.setText("Summary - All Time:");

        refreshPage(LocalDate.MIN, LocalDate.now());
    }

    /**
     * Updates the summary label and refreshes the page to show data for the custom dates entered.
     * @throws SQLException If the database query fails
     */
    public void customDate() throws SQLException {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        boolean invalidDate = fromDate == null || toDate == null || fromDate.isAfter(toDate) || toDate.isBefore(LocalDate.now());

        if (invalidDate) {
            invalidDateLabel.setVisible(true);
        } else {
            invalidDateLabel.setVisible(false);
            summaryLabel.setText("Summary - Custom Range:");

            // Refreshes every section, matching the preset period buttons. Updating only the totals, bar chart
            // and transaction list left the pie charts and balance chart showing the previously selected range.
            refreshPage(fromDate, toDate);
        }
    }

    /**
     * Fills the four summary tiles. Reads the same AccountSummary as before, but takes its fields directly
     * rather than its toString(), which packed all four figures into one tab-separated line.
     */
    private void setTotals(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        AccountSummary currentAccountSummary = DatabaseManager.getInstance().getSummary(currentAccount.getAccountID(),
                sinceDate, toDate);

        incomeValueLabel.setText(formatAmount(currentAccountSummary.income));
        expensesValueLabel.setText(formatAmount(currentAccountSummary.expenses));
        setSignedAmount(netValueLabel, currentAccountSummary.net);
        setSignedAmount(avgValueLabel, currentAccountSummary.avgNetPerMonth);
    }

    /**
     * Plots the income and expense bar chart for the given date range.
     * @param sinceDate Earliest date to grab data from (LocalDate)
     * @param toDate Latest date to grab data from (LocalDate)
     */
    private void plotIncomeExpenseBarChart(LocalDate sinceDate, LocalDate toDate) {
        removeExistingChart();

        Map<String, Map<String, Double>> monthlyData = DatabaseManager.getInstance().getMonthlyIncomeExpenseByDateRange(
                currentAccount.getAccountID(), sinceDate, toDate);

        Map<String, Double> incomeValues = monthlyData.get("Income");
        Map<String, Double> expenseValues = monthlyData.get("Expense");

        Set<String> allMonths = getAllMonths(incomeValues, expenseValues);

        barChart = createBarChart();

        XYChart.Series<Number, String> incomeSeries = createSeriesFromMap(incomeValues, allMonths, "Income");
        XYChart.Series<Number, String> expenseSeries = createSeriesFromMap(expenseValues, allMonths, "Expenses");

        barChart.getData().addAll(incomeSeries, expenseSeries);

        makeChartResponsive(barChart, WIDE_CHART_RATIO, WIDE_CHART_MIN_HEIGHT);
        barChartBox.getChildren().add(barChart);
    }

    /**
     * Removes any existing chart from the bar chart box.
     */
    private void removeExistingChart() {
        barChartBox.getChildren().clear();
    }

    /**
     * Returns a set of all months present in the income and expense data.
     * @param incomeValues Map of income values by month
     * @param expenseValues Map of expense values by month
     * @return Set of all months present in the income and expense data
     */
    private Set<String> getAllMonths(Map<String, Double> incomeValues, Map<String, Double> expenseValues) {
        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(incomeValues.keySet());
        allMonths.addAll(expenseValues.keySet());
        return allMonths;
    }

    /**
     * Handles the creation of a new bar chart for the plotIncomeExpenseBarChart method.
     * @return StackedBarChart<Number, String> corresponding to income vs. expenses over months
     */
    private StackedBarChart<Number, String> createBarChart() {
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Month");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Total Amount ($)");
        xAxis.setTickUnit(100);
        xAxis.setMinorTickVisible(false);

        StackedBarChart<Number, String> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Income vs. Expenses");
        chart.setLegendVisible(true);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        return chart;
    }

    /**
     * Creates a new series for the bar chart based on the given values and all months.
     * @param values Map containing the total values for each month
     * @param allMonths Set of all months with transactional data
     * @param seriesName Name of the series passed as a String
     * @return XYChart.Series<Number, String> corresponding to the given values and all months
     */
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

    /**
     * Plots a line chart of the balance history for the given date range.
     * @param sinceDate Start date of the range (LocalDate)
     * @param toDate End date of the range (LocalDate)
     */
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
            balanceChartBox.getChildren().add(createChartMessage("No balance data available for this period"));
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

        makeChartResponsive(balanceLineChart, WIDE_CHART_RATIO, WIDE_CHART_MIN_HEIGHT);
        balanceChartBox.getChildren().add(balanceLineChart);
    }

    /**
     * Clearing the chart's own container replaces the previous approach of matching placeholder labels by
     * their text, which broke silently if the wording ever changed.
     */
    private void removeExistingBalanceChart() {
        balanceChartBox.getChildren().clear();
    }


    /**
     * Creates maps of transaction types and purchase categories with their corresponding amounts for the given date
     * range and plots pie charts for each.
     * @param sinceDate Start date of the range (LocalDate)
     * @param toDate End date of the range (LocalDate)
     */
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

    /**
     * Plots the pie chart representing the type distribution of income transactions.
     * @param typeAmountMap Map of transaction types and their corresponding amounts
     */
    private void plotIncomePieChart(Map<String, Double> typeAmountMap) {
        ObservableList<PieChart.Data> incomePieChartData = FXCollections.observableArrayList();

        typeAmountMap.forEach((type, amount) -> {
            if (!Transaction.isExpense(type)) {
                incomePieChartData.add(new PieChart.Data(pluralizeType(type), amount / typeAmountMap.values().stream().mapToDouble(Double::doubleValue).sum()));
            }
        });

        if (incomePieChartData.isEmpty()) {
            pieChartRow.getChildren().add(createChartMessage("No income data available for this period"));
        } else {
            PieChart incomePieChart = new PieChart(incomePieChartData);
            incomePieChart.setTitle("Income by Type");
            incomePieChart.setLegendVisible(true);
            incomePieChart.setLabelsVisible(true);

            addPieChart(incomePieChart);
        }
    }

    /**
     * Places a pie chart in the shared row, giving it an equal share of the width and a height that tracks
     * that width so all three stay square-ish as the window grows.
     */
    private void addPieChart(PieChart pieChart) {
        makeChartResponsive(pieChart, PIE_CHART_RATIO, PIE_CHART_MIN_HEIGHT);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        pieChartRow.getChildren().add(pieChart);
    }

    /**
     * Plots the pie chart representing the type distribution of expense transactions.
     * @param typeAmountMap Map of transaction types and their corresponding amounts
     */
    private void plotExpensePieChart(Map<String, Double> typeAmountMap) {
        ObservableList<PieChart.Data> expensePieChartData = FXCollections.observableArrayList();

        typeAmountMap.forEach((type, amount) -> {
            if (Transaction.isExpense(type)) {
                expensePieChartData.add(new PieChart.Data(pluralizeType(type), amount / typeAmountMap.values().stream().mapToDouble(Double::doubleValue).sum()));
            }
        });

        if (expensePieChartData.isEmpty()) {
            pieChartRow.getChildren().add(createChartMessage("No expense data available for this period"));
        } else {
            PieChart expensePieChart = new PieChart(expensePieChartData);
            expensePieChart.setTitle("Expenses by Type");
            expensePieChart.setLegendVisible(true);
            expensePieChart.setLabelsVisible(true);

            addPieChart(expensePieChart);
        }
    }

    /**
     * Plots the pie chart representing the category distribution of purchase transactions (which are expenses).
     * @param purchaseCategoryAmountMap Map of purchase categories and their corresponding amounts
     */
    private void plotPurchasePieChart(Map<String, Double> purchaseCategoryAmountMap) {
        ObservableList<PieChart.Data> purchasePieChartData = FXCollections.observableArrayList();

        purchaseCategoryAmountMap.forEach((category, amount) -> {
            purchasePieChartData.add(new PieChart.Data(category, amount));
        });

        if (purchasePieChartData.isEmpty()) {
            purchaseChartBox.getChildren().add(createChartMessage("No purchase data available for this period"));
        } else {
            PieChart purchasePieChart = new PieChart(purchasePieChartData);
            purchasePieChart.setTitle("Purchases by Category");
            purchasePieChart.setLegendVisible(true);
            purchasePieChart.setLabelsVisible(true);

            addPurchasePieChart(purchasePieChart);
        }
    }

    /**
     * Places the purchases pie on its own row beneath the income and expense pair. It has the most categories
     * of the three, so sharing a row left it too small to read at the default window size.
     */
    private void addPurchasePieChart(PieChart pieChart) {
        makeChartResponsive(pieChart, PURCHASE_CHART_RATIO, PURCHASE_CHART_MIN_HEIGHT);

        // Bound after makeChartResponsive, which sets maxWidth directly: a bound property cannot then be set.
        pieChart.maxWidthProperty().bind(
                analyticsScrollPane.widthProperty().multiply(PURCHASE_CHART_WIDTH_SHARE));

        purchaseChartBox.getChildren().add(pieChart);
    }

    /**
     * Removes any existing pie charts from the UI.
     */
    private void removeExistingPieCharts() {
        pieChartRow.getChildren().clear();
        purchaseChartBox.getChildren().clear();
    }

    /**
     * Pluralizes the given transaction type.
     * @param type The transaction type to pluralize
     * @return The pluralized transaction type
     */
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


    /**
     * Lists the largest transactions of the period as one row each, rather than as a single block of
     * newline-separated toString() output, so each amount can be colored by direction.
     * @param sinceDate The start date of the period (LocalDate)
     * @param toDate The end date of the period (LocalDate)
     */
    private void setTopTransactions(LocalDate sinceDate, LocalDate toDate) {
        topTransactionsList.getChildren().clear();

        ObservableList<Transaction> topFiveList = DatabaseManager.getInstance().getTopTransactionsByDateRange(
                currentAccount.getAccountID(), sinceDate, toDate, 5);

        if (topFiveList.isEmpty()) {
            topTransactionsList.getChildren().add(createChartMessage("No transactions found for this period."));
            return;
        }

        for (Transaction transaction : topFiveList) {
            topTransactionsList.getChildren().add(createTransactionRow(transaction));
        }
    }

    /** Builds one transaction row: type and date on the left, the amount pushed to the right edge.
     * @param transaction The transaction to create a row for
     * @return The HBox containing the transaction row
     */
    private HBox createTransactionRow(Transaction transaction) {
        Label typeLabel = new Label(describeType(transaction));

        Label dateLabel = new Label(transaction.getDate().toString());
        dateLabel.getStyleClass().add("stat-label");

        // Absorbs the leftover width so the amount stays flush right at any window size.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(formatAmount(transaction.getAmount()));
        amountLabel.getStyleClass().addAll("text-bold",
                Transaction.isExpense(transaction.getType()) ? "amount-expense" : "amount-income");

        HBox row = new HBox(typeLabel, dateLabel, spacer, amountLabel);
        row.getStyleClass().add("transaction-row");
        return row;
    }

    /**
     * Purchases carry a category worth showing; every other type is described by its type alone.
     * @param transaction The transaction to describe
     * @return The description of the transaction type
     */
    private String describeType(Transaction transaction) {
        return Transaction.requiresCategory(transaction.getType()) && transaction.getCategory() != null
                ? transaction.getType() + " - " + transaction.getCategory()
                : transaction.getType();
    }

    /**
     * Refreshes the totals, charts, and top transactions based on the dates given.
     * @param sinceDate The start date of the period (LocalDate)
     * @param toDate The end date of the period (LocalDate)
     * @throws SQLException If there is an error accessing the database when these methods call DatabaseManager methods
     */
    private void refreshPage(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        setTotals(sinceDate, toDate);
        plotIncomeExpenseBarChart(sinceDate, toDate);
        plotPieCharts(sinceDate, toDate);
        plotBalanceLineChart(sinceDate, toDate);
        setTopTransactions(sinceDate, toDate);
    }

}
