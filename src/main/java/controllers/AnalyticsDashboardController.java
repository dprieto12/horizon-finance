package controllers;

import database.DatabaseManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
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

// TODO: Document
// TODO: Stylize


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

    @FXML
    private ScrollPane analyticsScrollPane;

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
    private Label dataErrorLabel;

    @FXML
    private Label incomeValueLabel;

    @FXML
    private Label expensesValueLabel;

    @FXML
    private Label netValueLabel;

    @FXML
    private Label avgValueLabel;

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

    private Account currentAccount = ApplicationState.getCurrentAccount();

    private StackedBarChart<Number, String> barChart;

    private AreaChart<String, Number> balanceLineChart;

    @FXML
    public void initialize() {
        SceneManager.setTitle("Analytics");

        updateAccountInfoLabel();
        bindResponsiveSpacing();
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

        makeChartResponsive(barChart, WIDE_CHART_RATIO, WIDE_CHART_MIN_HEIGHT);
        barChartBox.getChildren().add(barChart);
    }

    private void removeExistingChart() {
        barChartBox.getChildren().clear();
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

    /**
     * Bar colors are no longer set here. They come from styles.css, which targets the series classes JavaFX
     * already applies (.default-color0 for the income series, .default-color1 for expenses). An inline style
     * would outrank the stylesheet, and it only reached the bars themselves -- the legend swatches kept the
     * theme's default colors and disagreed with them.
     */
    private void styleBars() {
        for (XYChart.Series<Number, String> series : barChart.getData()) {
            for (XYChart.Data<Number, String> data : series.getData()) {
                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    addTooltip(node, series.getName(), data.getXValue());
                }
            }
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

    private void removeExistingPieCharts() {
        pieChartRow.getChildren().clear();
        purchaseChartBox.getChildren().clear();
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


    /**
     * Lists the largest transactions of the period as one row each, rather than as a single block of
     * newline-separated toString() output, so each amount can be colored by direction.
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

    /** Builds one transaction row: type and date on the left, the amount pushed to the right edge. */
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

    /** Purchases carry a category worth showing; every other type is described by its type alone. */
    private String describeType(Transaction transaction) {
        return Transaction.requiresCategory(transaction.getType()) && transaction.getCategory() != null
                ? transaction.getType() + " - " + transaction.getCategory()
                : transaction.getType();
    }

    private void refreshPage(LocalDate sinceDate, LocalDate toDate) throws SQLException {
        setTotals(sinceDate, toDate);
        plotIncomeExpenseBarChart(sinceDate, toDate);
        plotPieCharts(sinceDate, toDate);
        plotBalanceLineChart(sinceDate, toDate);
        setTopTransactions(sinceDate, toDate);
    }

}
