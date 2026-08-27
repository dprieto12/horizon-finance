package models;

public class AccountSummary {
    public final double income;
    public final double expenses;
    public final double net;
    public final double avgNetPerMonth;

    public AccountSummary(double income, double expenses, double net, double avgNetPerMonth) {
        this.income = income;
        this.expenses = expenses;
        this.net = net;
        this.avgNetPerMonth = avgNetPerMonth;
    }

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
