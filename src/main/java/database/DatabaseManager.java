package database;

import models.Account;
import models.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

// TODO: Review documentation
// TODO: Implement a shared DatabaseManager instance along with a public static synchronized getInstance() method

/**
 * Manages all interactions with the horizon_database SQLite database. Responsible for building the database
 * and performing CRUD operations on both the accounts and transactions tables. Converts SQL rows into Account
 * and Transaction objects for use in Main, and accepts those objects to persist changes back to the database.
 */
public class DatabaseManager {
    private static String databaseUrl;

    /**
     * Upon instantiation, sets the databaseUrl and constructs the database.
     */
    public DatabaseManager() {
        databaseUrl = "jdbc:sqlite:horizon_database.db";
        buildDatabase();
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
     * @return ArrayList of Transaction objects
     */
    public ArrayList<Transaction> getTransactions(int accountID) {
        String query = "SELECT * FROM transactions WHERE account_id = ? ORDER BY date, transaction_id";
        return queryTransactions(query, accountID);
    }

    /**
     * Returns all transactions for a given account in a specific month and year.
     * @param accountID Account to retrieve transactions for
     * @param month     Month as integer (1–12), validated by caller
     * @param year      Four-digit year
     * @return ArrayList of Transaction objects
     */
    public ArrayList<Transaction> getTransactionsByMonth(int accountID, int month, int year) {
        String monthStr = String.format("%04d-%02d", year, month);
        String query = "SELECT * FROM transactions WHERE account_id = ? " +
                "AND strftime('%Y-%m', date) = ? ORDER BY date, transaction_id";
        return queryTransactions(query, accountID, monthStr);
    }

    /**
     * Returns all transactions for a given account matching a specific type.
     * @param accountID Account to retrieve transactions for
     * @param type      Transaction type to filter by (use Transaction.TYPE_* constants)
     * @return ArrayList of Transaction objects
     */
    public ArrayList<Transaction> getTransactionsByType(int accountID, String type) {
        String query = "SELECT * FROM transactions WHERE account_id = ? AND type = ? " +
                "ORDER BY date, transaction_id";
        return queryTransactions(query, accountID, type);
    }

    /**
     * Returns all purchase transactions for a given account matching a specific category.
     * Only meaningful for "purchase" type transactions — all other types have a null category.
     * @param accountID Account to retrieve transactions for
     * @param category  Purchase category to filter by (e.g. "Food", "Entertainment")
     * @return ArrayList of Transaction objects
     */
    public ArrayList<Transaction> getTransactionsByCategory(int accountID, String category) {
        String query = "SELECT * FROM transactions WHERE account_id = ? AND category = ? " +
                "ORDER BY date, transaction_id";
        return queryTransactions(query, accountID, category);
    }

    /**
     * Private helper that executes a parameterised transaction SELECT and maps each row to a Transaction object.
     *
     * accountID always fills position 1. Any additional string parameters are set sequentially from position 2
     * onward — callers must ensure the number of stringParams matches the remaining ? placeholders exactly.
     *
     * Using varargs (String...) means callers pass only the params they need, and each is assigned the correct
     * positional index automatically — avoiding the previous bug where named params (param2, param3...) were
     * passed at the wrong index and silently returned empty results.
     */
    private ArrayList<Transaction> queryTransactions(String query, int accountID, String... stringParams) {
        ArrayList<Transaction> transactions = new ArrayList<>();
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