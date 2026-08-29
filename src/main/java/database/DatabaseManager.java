package database;

import models.Account;
import models.AccountSummary;
import models.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// TODO: Review documentation

/**
 * Manages all interactions with the horizon_database SQLite database. Responsible for building the database
 * and performing CRUD operations on both the accounts and transactions tables. Converts SQL rows into Account
 * and Transaction objects for use in Main, and accepts those objects to persist changes back to the database.
 */
public class DatabaseManager {
    private static String databaseUrl;

    private static DatabaseManager instance;

    /**
     * Upon instantiation, sets the databaseUrl and constructs the database.
     */
    private DatabaseManager() {
        String projectRoot = System.getProperty("user.dir");
        databaseUrl = "jdbc:sqlite:" + projectRoot + "/horizon_database.db";
        buildDatabase();
    }

    /**
     * Returns the singleton DatabaseManager instance, which is a private static field held by the class. If the
     * instance does not exist, it is created.
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Database Setup
    // -------------------------------------------------------------------------

    /**
     * Creates the database tables if they do not already exist. IF NOT EXISTS guards ensure no data is lost
     * on subsequent runs. The initial connection attempt is made implicitly by createAccountsTable().
     */
    private void buildDatabase() {
        createAccountsTable();
        createTransactionsTable();
    }

    private void createAccountsTable() {
        String query = """
                CREATE TABLE IF NOT EXISTS accounts(
                    account_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_name TEXT    NOT NULL,
                    first_name   TEXT    NOT NULL,
                    last_name    TEXT    NOT NULL,
                    balance      REAL    NOT NULL
                );""";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            System.out.println("Connected to horizon_database.");
        } catch (SQLException e) {
            System.err.println("Failed to create accounts table: " + e.getMessage());
        }
    }

    private void createTransactionsTable() {
        // category is nullable — only populated for "purchase" type transactions
        String query = """
                CREATE TABLE IF NOT EXISTS transactions(
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_id     INTEGER NOT NULL,
                    amount         REAL    NOT NULL,
                    type           TEXT,
                    category       TEXT,
                    date           TEXT    NOT NULL
                );""";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            System.err.println("Failed to create transactions table: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Account CRUD
    // -------------------------------------------------------------------------

    /**
     * Inserts a new account row into the accounts table.
     * NOTE: Does not accept an Account object — a valid account_id does not exist until the database generates
     * one via AUTOINCREMENT, so constructing an Account beforehand would produce an object with a meaningless
     * placeholder ID.
     */
    public void createNewAccount(String accountName, String firstName, String lastName, double balance) {
        String query = "INSERT INTO accounts (account_name, first_name, last_name, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setDouble(4, balance);
            pstmt.executeUpdate();
            System.out.println("Created account \"" + accountName + "\" for " + firstName + " " + lastName + ".");
        } catch (SQLException e) {
            System.err.println("Failed to create account: " + e.getMessage());
        }
    }

    /**
     * Updates an existing account row using data held in the provided Account object.
     * NOTE: Balance is included so transaction-driven changes are correctly persisted when this is called
     * after a refresh. Direct balance manipulation is intentionally not exposed through the UI.
     * NOTE: Space before WHERE is required — its absence causes a silent SQL syntax failure.
     * @param account Modified Account object to persist
     */
    public void updateAccount(Account account) {
        String query = "UPDATE accounts SET account_name = ?, first_name = ?, last_name = ?, balance = ? " +
                "WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, account.getAccountName());
            pstmt.setString(2, account.getFirstName());
            pstmt.setString(3, account.getLastName());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setInt(5, account.getAccountID());
            pstmt.executeUpdate();
            System.out.println("Account updated successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to update account: " + e.getMessage());
        }
    }

    /**
     * Deletes an account and all of its associated transactions from the database.
     * Transactions are deleted first to avoid orphaned rows.
     * @param account Account to delete
     */
    public void deleteAccount(Account account) {
        String deleteTransactions = "DELETE FROM transactions WHERE account_id = ?";
        String deleteAccount      = "DELETE FROM accounts WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement dropTxns = conn.prepareStatement(deleteTransactions);
             PreparedStatement dropAcc  = conn.prepareStatement(deleteAccount)) {
            dropTxns.setInt(1, account.getAccountID());
            dropTxns.executeUpdate();
            dropAcc.setInt(1, account.getAccountID());
            dropAcc.executeUpdate();
            System.out.println("Deleted account \"" + account.getAccountName() + "\" and all associated transactions.");
        } catch (SQLException e) {
            System.err.println("Failed to delete account: " + e.getMessage());
        }
    }

    /**
     * Returns all accounts in the database as Account objects, ordered by account_id.
     * ORDER BY account_id ensures the numbered display in Main is always consistent —
     * without it, SQLite does not guarantee any particular row order.
     * Returns an empty list (never null) if no accounts exist or on failure.
     * @return ArrayList of Account objects
     */
    public ArrayList<Account> getAccountList() {
        String query = "SELECT account_id, account_name, first_name, last_name, balance " +
                "FROM accounts ORDER BY account_id";
        ArrayList<Account> accounts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve accounts: " + e.getMessage());
        }
        return accounts;
    }

    /**
     * Returns a single account by its ID, or null if not found.
     * Used by Main to refresh the current account after transactions modify the balance,
     * avoiding the overhead of fetching all accounts just to update one.
     * @param accountID The account_id to look up
     * @return Account object, or null if not found
     */
    public Account getAccount(int accountID) {
        String query = "SELECT account_id, account_name, first_name, last_name, balance " +
                "FROM accounts WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve account: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Transaction CRUD
    // -------------------------------------------------------------------------

    /**
     * Inserts a new transaction and updates the associated account balance, wrapped in a single SQL
     * transaction for atomicity. If the balance update fails after the INSERT (or vice versa), both
     * operations are rolled back together, preventing the database from reaching an inconsistent state.
     *
     * How this works: setAutoCommit(false) means neither statement is committed until conn.commit()
     * is explicitly called. If a SQLException is thrown before commit(), the try-with-resources block
     * closes the connection, which rolls back all uncommitted changes automatically.
     *
     * Category must be non-null only for "purchase" type — pass null for all other types.
     *
     * @param accountID Account to associate the transaction with
     * @param amount    Positive transaction amount (validated by caller)
     * @param type      Transaction type constant from Transaction (e.g. Transaction.TYPE_PURCHASE)
     * @param category  Spending category for purchases; null for all other types
     * @param date      Date of the transaction
     */
    public void createTransaction(int accountID, double amount, String type, String category, LocalDate date) {
        String insertTxn     = "INSERT INTO transactions (account_id, amount, type, category, date) VALUES (?, ?, ?, ?, ?)";
        String updateBalance = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement addTxn    = conn.prepareStatement(insertTxn);
             PreparedStatement updateAcc = conn.prepareStatement(updateBalance)) {

            conn.setAutoCommit(false);

            addTxn.setInt(1, accountID);
            addTxn.setDouble(2, amount);
            addTxn.setString(3, type);

            // setString with null is unreliable across JDBC drivers — setNull is the correct approach
            if (category != null) {
                addTxn.setString(4, category);
            } else {
                addTxn.setNull(4, Types.VARCHAR);
            }

            addTxn.setString(5, date.toString());
            addTxn.executeUpdate();

            // Expenses subtract from balance, income adds
            double signedAmount = Transaction.isExpense(type) ? -amount : amount;
            updateAcc.setDouble(1, signedAmount);
            updateAcc.setInt(2, accountID);
            updateAcc.executeUpdate();

            conn.commit();
            System.out.println("Transaction added: " + type + " of $" + String.format("%.2f", amount));

        } catch (SQLException e) {
            // Connection close (via try-with-resources) rolls back uncommitted changes automatically
            System.err.println("Failed to create transaction — changes rolled back: " + e.getMessage());
        }
    }

    /**
     * Deletes a transaction and reverses its effect on the account balance, wrapped in a single SQL
     * transaction for atomicity. If either operation fails, both are rolled back together.
     * @param transaction Transaction to delete
     */
    public void deleteTransaction(Transaction transaction) {
        String deleteTxn      = "DELETE FROM transactions WHERE transaction_id = ?";
        String reverseBalance = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement del     = conn.prepareStatement(deleteTxn);
             PreparedStatement reverse = conn.prepareStatement(reverseBalance)) {

            conn.setAutoCommit(false);

            del.setInt(1, transaction.getTransactionID());
            del.executeUpdate();

            // Reverse the original balance effect
            double reversal = Transaction.isExpense(transaction.getType())
                    ? transaction.getAmount()   // was subtracted — add back
                    : -transaction.getAmount(); // was added — subtract back
            reverse.setDouble(1, reversal);
            reverse.setInt(2, transaction.getAccountID());
            reverse.executeUpdate();

            conn.commit();
            System.out.println("Deleted transaction TID#" + transaction.getTransactionID() + ".");

        } catch (SQLException e) {
            System.err.println("Failed to delete transaction — changes rolled back: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Transaction Queries / Analytics
    // -------------------------------------------------------------------------

    /**
     * Returns all transactions for a given account, ordered by date then transaction_id.
     * @param accountID Account to retrieve transactions for
     * @return ObservableList of Transaction objects
     */
    public ObservableList<Transaction> getTransactions(int accountID) {
        String query = "SELECT * FROM transactions WHERE account_id = ? ORDER BY date, transaction_id";
        return queryTransactions(query, accountID);
    }

    // TODO: Rework methods to use two dates instead of one


    /**
     * Returns all transactions for a given account within a specific date range.
     * @param accountID Account to retrieve transactions for
     * @param sinceDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return ObservableList of Transaction objects
     */
    public ObservableList<Transaction> getTransactionsByDateRange(int accountID, LocalDate sinceDate, LocalDate toDate) {
        String query = "SELECT * FROM transactions WHERE account_id = ? AND date >= ? AND date <= ? ORDER BY date, transaction_id";
        return queryTransactions(query, accountID, sinceDate.toString(), toDate.toString());
    }

    /**
     * Returns a map of transaction types to their total amounts for a given account within a specific date range.
     * @param accountID Account to retrieve transactions for
     * @param sinceDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return Map where key is transaction type and value is sum of amounts
     */
    public Map<String, Double> getTransactionsByTypeAndDateRange(int accountID, LocalDate sinceDate, LocalDate toDate) {
        String query = "SELECT type, SUM(amount) as total FROM transactions " +
                "WHERE account_id = ? AND date >= ? AND date <= ? GROUP BY type";
        Map<String, Double> result = new java.util.HashMap<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountID);
            pstmt.setString(2, sinceDate.toString());
            pstmt.setString(3, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("type"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Returns a map of purchase categories to their total amounts for a given account within a specific date range.
     * Only includes "purchase" type transactions.
     * @param accountID Account to retrieve transactions for
     * @param sinceDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return Map where key is category and value is sum of amounts
     */
    public Map<String, Double> getPurchasesByCategoryAndDateRange(int accountID, LocalDate sinceDate, LocalDate toDate) {
        String query = "SELECT category, SUM(amount) as total FROM transactions " +
                "WHERE account_id = ? AND date >= ? AND date <= ? AND type = 'Purchase' GROUP BY category";
        Map<String, Double> result = new java.util.HashMap<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountID);
            pstmt.setString(2, sinceDate.toString());
            pstmt.setString(3, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Returns monthly income and expense totals for a given account within a specific date range.
     * @param accountID Account to retrieve transactions for
     * @param sinceDate Start date (inclusive), or null for all-time
     * @param toDate End date (inclusive)
     * @return Map with "Income" and "Expense" keys, each mapping to a month->amount map
     */
    public Map<String, Map<String, Double>> getMonthlyIncomeExpenseByDateRange(int accountID, LocalDate sinceDate, LocalDate toDate) {
        String query;
        if (sinceDate == null) {
            query = "SELECT strftime('%Y-%m', date) as month, type, SUM(amount) as total " +
                    "FROM transactions WHERE account_id = ? AND date <= ? " +
                    "GROUP BY month, type";
        } else {
            query = "SELECT strftime('%Y-%m', date) as month, type, SUM(amount) as total " +
                    "FROM transactions WHERE account_id = ? AND date >= ? AND date <= ? " +
                    "GROUP BY month, type";
        }

        Map<String, Map<String, Double>> result = new java.util.HashMap<>();
        result.put("Income", new java.util.HashMap<>());
        result.put("Expense", new java.util.HashMap<>());

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountID);
            if (sinceDate == null) {
                pstmt.setString(2, toDate.toString());
            } else {
                pstmt.setString(2, sinceDate.toString());
                pstmt.setString(3, toDate.toString());
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String month = rs.getString("month");
                String type = rs.getString("type");
                double amount = rs.getDouble("total");

                if (Transaction.isExpense(type)) {
                    result.get("Expense").merge(month, amount, Double::sum);
                } else {
                    result.get("Income").merge(month, amount, Double::sum);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Returns all purchase transactions for a given account matching a specific category.
     * Only meaningful for "purchase" type transactions — all other types have a null category.
     * @param accountID Account to retrieve transactions for
     * @param category  Purchase category to filter by (e.g. "Food", "Entertainment")
     * @return ObservableList of Transaction objects
     */
    public ObservableList<Transaction> getTransactionsByCategory(int accountID, String category) {
        String query = "SELECT * FROM transactions WHERE account_id = ? AND category = ? " +
                "ORDER BY date, transaction_id";
        return queryTransactions(query, accountID, category);
    }

    public AccountSummary getSummary(int accountID, LocalDate sinceDate, LocalDate toDate) throws SQLException {
        String summaryQuery = "SELECT " +
                "SUM(CASE WHEN type IN('Wages', 'Sale', 'Gift', 'Refund', 'Interest') THEN amount ELSE 0 END) as income, " +
                "SUM(CASE WHEN type NOT IN('Wages', 'Sale', 'Gift', 'Refund', 'Interest') THEN amount ELSE 0 END) as expenses " +
                "FROM transactions " +
                "WHERE account_id = ? AND date >= ? AND date <= ?";

        String monthQuery = "SELECT COUNT(DISTINCT strftime('%Y-%m', date)) as active_months " +
                "FROM transactions " +
                "WHERE account_id = ? AND date >= ? AND date <= ?";

        try (Connection conn = DriverManager.getConnection(databaseUrl);
            PreparedStatement summaryStmt = conn.prepareStatement(summaryQuery)) {
            summaryStmt.setInt(1, accountID);
            summaryStmt.setString(2, sinceDate.toString());
            summaryStmt.setString(3, toDate.toString());

            ResultSet summaryRs = summaryStmt.executeQuery();

            PreparedStatement monthPstmt = conn.prepareStatement(monthQuery);
            monthPstmt.setInt(1, accountID);
            monthPstmt.setString(2, sinceDate.toString());
            monthPstmt.setString(3, toDate.toString());

            ResultSet monthRs = monthPstmt.executeQuery();

            double income = summaryRs.getDouble("income");
            double expenses = summaryRs.getDouble("expenses");
            double net = income - expenses;
            double avgNetPerMonth = net / monthRs.getDouble("active_months");

            return new AccountSummary(income, expenses, net, avgNetPerMonth);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the top transactions by amount for a given account within a specific date range.
     * @param accountID Account to retrieve transactions for
     * @param sinceDate Start date (inclusive), or null for all-time
     * @param toDate End date (inclusive)
     * @param limit Maximum number of transactions to return
     * @return ObservableList of Transaction objects, sorted by amount descending
     */
    public ObservableList<Transaction> getTopTransactionsByDateRange(int accountID, LocalDate sinceDate, LocalDate toDate, int limit) {
        String query;
        if (sinceDate == null) {
            query = "SELECT * FROM transactions WHERE account_id = ? AND date <= ? ORDER BY amount DESC, transaction_id LIMIT ?";
            return queryTransactionsWithLimit(query, accountID, toDate.toString(), String.valueOf(limit));
        } else {
            query = "SELECT * FROM transactions WHERE account_id = ? AND date >= ? AND date <= ? ORDER BY amount DESC, transaction_id LIMIT ?";
            return queryTransactionsWithLimit(query, accountID, sinceDate.toString(), toDate.toString(), String.valueOf(limit));
        }
    }

    /**
     * Returns balance history for an account as a map of dates to balance values.
     * This handles the case where transactions can be added with past dates by calculating
     * the running balance chronologically from the initial balance.
     * @param accountID Account to retrieve balance history for
     * @return Map where key is date and value is balance on that date
     */
    public Map<LocalDate, Double> getBalanceHistory(int accountID) {
        // Get current account balance
        Account account = getAccount(accountID);
        if (account == null) {
            return new java.util.HashMap<>();
        }
        double currentBalance = account.getBalance();

        // Get all transactions sorted by date
        String query = "SELECT * FROM transactions WHERE account_id = ? ORDER BY date, transaction_id";
        ObservableList<Transaction> allTransactions = queryTransactions(query, accountID);

        // Calculate the net effect of all transactions
        double netTransactionEffect = 0.0;
        for (Transaction t : allTransactions) {
            double signedAmount = Transaction.isExpense(t.getType()) ? -t.getAmount() : t.getAmount();
            netTransactionEffect += signedAmount;
        }

        // Initial balance is current balance minus all transaction effects
        double runningBalance = currentBalance - netTransactionEffect;

        // Build balance history by walking through transactions chronologically
        Map<LocalDate, Double> balanceHistory = new java.util.TreeMap<>();
        balanceHistory.put(LocalDate.MIN, runningBalance); // Starting balance

        for (Transaction t : allTransactions) {
            double signedAmount = Transaction.isExpense(t.getType()) ? -t.getAmount() : t.getAmount();
            runningBalance += signedAmount;
            balanceHistory.put(t.getDate(), runningBalance);
        }

        return balanceHistory;
    }

    /**
     * Private helper that executes a parameterized transaction SELECT and maps each row to a Transaction object.
     *
     * accountID always fills position 1. Any additional string parameters are set sequentially from position 2
     * onward — callers must ensure the number of stringParams matches the remaining ? placeholders exactly.
     *
     * Using varargs (String...) means callers pass only the params they need, and each is assigned the correct
     * positional index automatically — avoiding the previous bug where named params (param2, param3...) were
     * passed at the wrong index and silently returned empty results.
     */
    private ObservableList<Transaction> queryTransactions(String query, int accountID, String... stringParams) {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, accountID);
            for (int i = 0; i < stringParams.length; i++) {
                pstmt.setString(i + 2, stringParams[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve transactions: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Private helper that executes a parameterized transaction SELECT with an integer LIMIT parameter.
     * Similar to queryTransactions but supports integer parameters for LIMIT clauses.
     */
    private ObservableList<Transaction> queryTransactionsWithLimit(String query, int accountID, String... stringParams) {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, accountID);
            for (int i = 0; i < stringParams.length; i++) {
                String param = stringParams[i];
                if (i == stringParams.length - 1 && param.matches("\\d+")) {
                    pstmt.setInt(i + 2, Integer.parseInt(param));
                } else {
                    pstmt.setString(i + 2, param);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve transactions: " + e.getMessage());
        }
        return transactions;
    }

    // -------------------------------------------------------------------------
    // Row Mapping Helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current row of a ResultSet to an Account object.
     * Extracted to avoid duplicating column-name strings across getAccountList() and getAccount().
     */
    private Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("account_id"),
                rs.getString("account_name"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDouble("balance")
        );
    }

    /**
     * Maps the current row of a ResultSet to a Transaction object.
     * category may be null for non-purchase transactions — getString returns null for SQL NULL, which is correct.
     */
    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("account_id"),
                rs.getDouble("amount"),
                rs.getString("type"),
                rs.getString("category"),
                LocalDate.parse(rs.getString("date"))
        );
    }
}