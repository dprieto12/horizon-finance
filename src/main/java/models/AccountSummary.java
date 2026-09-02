package models;

/**
 * <p>Class representing a summary of a user's account. Used by the AnalyticsDashboardController and DatabaseManager in
 * order to communicate data between the client and the database. DatabaseManager uses aggregated data from the
 * transactions table to create AccountSummary objects, providing an overview of total income, expenses, net income,
 * and net income per month across the chosen time period.</p>
 *
 * <p>Along with Transaction and Account, AccountSummary is one of the three models used by the application. It is
 * only used to represent SQL Database information, assisting in passing that information back and forth but never
 * being the key source for that data.</p>
 */

public class AccountSummary {
    // Final fields, since they are calculated from the database
    // Can remain public since the summary can either be provided by the toString() or values can be retrieved directly
    public final double income;
    public final double expenses;
    public final double net;
    public final double avgNetPerMonth;

    /**
     * Creates an AccountSummary object with the given values, setting fields directly to each parameter passed.
     * @param income Total income across the time period (double).
     * @param expenses Total expenses across the time period (double).
     * @param net Total net income across the time period (double).
     * @param avgNetPerMonth Average net income per month across the time period (double).
     */
    public AccountSummary(double income, double expenses, double net, double avgNetPerMonth) {
        this.income = income;
        this.expenses = expenses;
        this.net = net;
        this.avgNetPerMonth = avgNetPerMonth;
    }

    /**
     * Provides a simple formatted string representation of the AccountSummary object, providing the 4 values in a
     * manner that is easily readable and applicable.
     * @return String representation of the AccountSummary object.
     */

    @Override
    public String toString() {
        String summaryLine = "Income: $" + String.format("%.2f", income) +
                "\t\tExpenses: $" + String.format("%.2f", expenses) +
                "\t\tNet: ";

        if (net >= 0) {
            summaryLine += "+$" + String.format("%.2f", net);
        } else {
            summaryLine += "-$" + String.format("%.2f", -1 * net);
        }

        summaryLine += "\t\tNet Avg / Month: ";

        if (avgNetPerMonth >= 0) {
            summaryLine += "+$" + String.format("%.2f", avgNetPerMonth);
        } else {
            summaryLine += "-$" + String.format("%.2f", -1 * avgNetPerMonth);
        }

        return summaryLine;
    }
}
