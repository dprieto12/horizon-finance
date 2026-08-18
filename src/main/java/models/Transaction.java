package models;

import java.time.LocalDate;

// TODO: Review documentation for this class

/**
 * Represents a single transaction associated with an account. Used by Main and DatabaseManager to communicate
 * transaction data between the client and the database. Category is only applicable to "purchase" transactions
 * and will be null for all other types.
 */
public class Transaction {
    private int transactionID;
    private int accountID;
    private double amount;
    private String type;
    private String category; // Only populated for "purchase" type, null otherwise
    private LocalDate date;

    // Expense types — subtract from balance
    public static final String TYPE_PURCHASE = "purchase";
    public static final String TYPE_TRANSFER = "transfer";
    public static final String TYPE_WITHDRAWAL = "withdrawal";
    public static final String TYPE_BILL = "bill";
    public static final String TYPE_FEE = "fee";

    // Income types — add to balance
    public static final String TYPE_WAGES = "wages";
    public static final String TYPE_SALE = "sale";
    public static final String TYPE_GIFT = "gift";
    public static final String TYPE_REFUND = "refund";
    public static final String TYPE_INTEREST = "interest";

    public static final String[] expenseTypes = {TYPE_PURCHASE, TYPE_TRANSFER, TYPE_WITHDRAWAL, TYPE_BILL, TYPE_FEE};
    public static final String[] incomeTypes = {TYPE_WAGES, TYPE_SALE, TYPE_GIFT, TYPE_REFUND, TYPE_INTEREST};

    // Purchase categories — only used when type is "purchase"
    public static final String[] purchaseCategories = {"Food & Groceries", "Personal", "School", "Entertainment",
            "Transportation", "Healthcare", "Housing", "Other"};

    /**
     * Full constructor — used when reading a transaction back from the database, where all fields including
     * the database-generated ID and date are known.
     */
    public Transaction(int transactionID, int accountID, double amount, String type, String category, LocalDate date) {
        this.transactionID = transactionID;
        this.accountID = accountID;
        this.amount = amount;
        this.type = type;
        this.category = category; // May be null for non-purchase types
        this.date = date;
    }

    /**
     * Constructor for creating a new transaction with a specific date. Used before inserting into the database,
     * so transactionID is not yet set.
     */
    public Transaction(int accountID, double amount, String type, String category, LocalDate date) {
        this.accountID = accountID;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    /**
     * Constructor for creating a new transaction defaulting to today's date.
     */
    public Transaction(int accountID, double amount, String type, String category) {
        this(accountID, amount, type, category, LocalDate.now());
    }

    /**
     * Returns true if the given transaction type subtracts from the account balance.
     * @param type Transaction type string
     * @return true if expense, false if income
     */
    public static boolean isExpense(String type) {
        for (String expenseType : expenseTypes) {
            if (expenseType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given type requires a category (currently only purchases).
     * @param type Transaction type string
     * @return true if category is required
     */
    public static boolean requiresCategory(String type) {
        return TYPE_PURCHASE.equals(type);
    }

    /**
     * Displays the transaction in a readable format. Category is only shown for purchase transactions.
     *
     * Example outputs:
     *   TID#4 | $25.00 | purchase - Food | 2026-05-11
     *   TID#5 | $500.00 | wages | 2026-05-15
     */
    @Override
    public String toString() {
        String typeDisplay = requiresCategory(type) && category != null
                ? type + " - " + category
                : type;
        return "TID#" + transactionID + " | $" + String.format("%.2f", amount)
                + " | " + typeDisplay + " | " + date;
    }

    public int getTransactionID() { return transactionID; }
    public int getAccountID() { return accountID; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
}