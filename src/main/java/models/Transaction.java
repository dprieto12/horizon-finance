package models;

import java.time.LocalDate;

/**
 * <p>Class representing a single transaction associated with an account. Used by Controllers and DatabaseManager in
 * order to communicate data between the client and the database. DatabaseManager converts SQL data stored in the
 * transactions table to Transaction objects and receives new Transaction objects to update the SQL database.</p>
 *
 * <p>Along with Account and AccountSummary, Transaction is one of the three models used by the application. It is
 * only used to represent SQL Database information, assisting in passing that information back and forth but never
 * being the key source for that data.</p>
 *
 * <p>Note: Transactions can only be created or deleted, and therefore have no setters. The constructor will
 * assign all fields initially, but those fields cannot be altered by the program aftter creation</p>
 *
 * <p>Category Field: Not all Transactions have the transaction type of purchase, but all purchases are
 * Transactions. Purchases also have categories, since the term "purchase" is very vague in relation to what a user
 * purchases. Ideally, purchases could be a child class of Transaction, but for the current implementation, it
 * was sufficient to group them in as just a type of Transaction. If a Transaction is not a purchase, the category field
 * will be null.<p/>
 */

public class Transaction {
    private int transactionID;
    private int accountID;
    private double amount;
    private String type;
    private String category; // Only populated for "purchase" type, null otherwise
    private LocalDate date;

    // Expense types — subtract from balance
    public static final String TYPE_PURCHASE = "Purchase";
    public static final String TYPE_TRANSFER = "Transfer";
    public static final String TYPE_WITHDRAWAL = "Withdrawal";
    public static final String TYPE_BILL = "Bill";
    public static final String TYPE_FEE = "Fee";

    // Income types — add to balance
    public static final String TYPE_WAGES = "Wages";
    public static final String TYPE_SALE = "Sale";
    public static final String TYPE_GIFT = "Gift";
    public static final String TYPE_REFUND = "Refund";
    public static final String TYPE_INTEREST = "Interest";

    // Arrays of income and expense types, necessary for checking transactions
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
     * @return true if Transaction is an expense, false if it is income
     */
    public static boolean isExpense(String type) {
        for (String expenseType : expenseTypes) {
            if (expenseType.equalsIgnoreCase(type)) {
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
        return TYPE_PURCHASE.equalsIgnoreCase(type);
    }

    /**
     * <p>Displays the transaction in a readable format. Category is only shown for purchase transactions.</p>
     *
     * <p>Example: "TID#4 | $25.00 | Purchase - Food | 2026-05-11" </p>
     */
    @Override
    public String toString() {
        String typeDisplay = requiresCategory(type) && category != null
                ? type + " - " + category
                : type;
        return "TID#" + transactionID + " | $" + String.format("%.2f", amount)
                + " | " + typeDisplay + " | " + date;
    }

    // Getters for each field

    /**
     * Returns the transaction ID.
     * @return Transaction ID within the database (int)
     */
    public int getTransactionID() { return transactionID; }

    /**
     * Returns the account ID that the transaction is associated with.
     * @return Account ID within the database (int)
     */
    public int getAccountID() { return accountID; }

    /**
     * Returns the transaction amount as a positive value. Whether this amount is added to or subtracted from the
     * account balance is determined by the transaction type (see isExpense()).
     * @return Transaction amount (double)
     */
    public double getAmount() { return amount; }

    /**
     * Returns the transaction type as a String, such as "Purchase", "Wages", etc. (see static type fields)
     * @return Transaction type (String)
     */
    public String getType() { return type; }

    /**
     * Returns the transaction's category as a String. If the transaction is a purchase, it will have a category.
     * Otherwise, this method will return null.
     * @return Transaction category (String) — null if the transaction is not a purchase
     */
    public String getCategory() { return category; }

    /**
     * <p>Returns the date the transaction was made as a LocalDate.</p>
     *
     * <p>Similar to the custom models like this Transaction class, the LocalDate class is used to model dates in the
     * database, allowing date operations to be performed easily while still being stored in the database as a string.</p>
     * @return LocalDate of the date the transaction was made.
     */
    public LocalDate getDate() { return date; }
}