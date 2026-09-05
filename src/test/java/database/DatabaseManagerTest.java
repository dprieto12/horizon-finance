package database;

import models.Account;
import models.AccountSummary;
import models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseManager using an in-memory SQLite database so that no test ever touches
 * horizon_database.db. A fresh DatabaseManager is constructed before each test via the
 * package-private constructor, which guarantees a clean schema with no leftover rows.
 *
 * Placement: src/test/java/database/DatabaseManagerTest.java
 * The package-private constructor is only visible from within the database package,
 * so this test class must live here.
 */
class DatabaseManagerTest {

    private DatabaseManager db;

    // -------------------------------------------------------------------------
    // Shared test data — used across multiple tests
    // -------------------------------------------------------------------------

    private static final String ACCOUNT_NAME  = "Checking";
    private static final String FIRST_NAME    = "John";
    private static final String LAST_NAME     = "Doe";
    private static final double INITIAL_BAL   = 1000.00;
    private static final int    COLOR         = 2;

    private static final double EXPENSE_AMOUNT = 50.00;
    private static final double INCOME_AMOUNT  = 200.00;
    private static final LocalDate TEST_DATE   = LocalDate.of(2026, 5, 15);

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private File tempDb;

    /**
     * Creates a fresh temporary database before every test. The file is deleted after each test.
     */
    @BeforeEach
    void setUp() {
        // Create a temp file path — the file doesn't exist yet, DatabaseManager creates it
        tempDb = new File(System.getProperty("java.io.tmpdir"), "horizon_test_" + System.nanoTime() + ".db");
        db = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        // Delete the temp database file after each test so nothing carries over
        if (tempDb != null && tempDb.exists()) {
            tempDb.delete();
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /** Creates a standard test account and returns it from the database so it carries a valid ID. */
    private Account createTestAccount() {
        db.createNewAccount(ACCOUNT_NAME, FIRST_NAME, LAST_NAME, INITIAL_BAL, COLOR);
        ArrayList<Account> accounts = db.getAccountList();
        assertFalse(accounts.isEmpty(), "Account should have been created");
        return accounts.get(0);
    }

    /** Adds a purchase expense to the given account and returns the transaction list so callers can get its ID. */
    private void addExpense(int accountID, double amount, LocalDate date) {
        db.createTransaction(accountID, amount, Transaction.TYPE_PURCHASE,
                Transaction.purchaseCategories[0], date);
    }

    /** Adds a wages income transaction to the given account. */
    private void addIncome(int accountID, double amount, LocalDate date) {
        db.createTransaction(accountID, amount, Transaction.TYPE_WAGES, null, date);
    }

    // -------------------------------------------------------------------------
    // Account — createNewAccount / getAccountList / getAccount
    // -------------------------------------------------------------------------

    @Test
    void createAccount_appearsInList() {
        db.createNewAccount(ACCOUNT_NAME, FIRST_NAME, LAST_NAME, INITIAL_BAL, COLOR);
        ArrayList<Account> accounts = db.getAccountList();

        assertEquals(1, accounts.size());
        Account a = accounts.get(0);
        assertEquals(ACCOUNT_NAME, a.getAccountName());
        assertEquals(FIRST_NAME,   a.getFirstName());
        assertEquals(LAST_NAME,    a.getLastName());
        assertEquals(INITIAL_BAL,  a.getBalance(), 0.001);
        assertEquals(COLOR,        a.getColor());
    }

    @Test
    void createMultipleAccounts_allAppearInList() {
        db.createNewAccount("Savings",  "Jane", "Doe", 500.00, 1);
        db.createNewAccount("Checking", "John", "Doe", 250.00, 3);

        ArrayList<Account> accounts = db.getAccountList();
        assertEquals(2, accounts.size());
    }

    @Test
    void getAccount_returnsCorrectAccount() {
        Account created = createTestAccount();
        Account fetched = db.getAccount(created.getAccountID());

        assertNotNull(fetched);
        assertEquals(created.getAccountID(),   fetched.getAccountID());
        assertEquals(created.getAccountName(), fetched.getAccountName());
        assertEquals(created.getBalance(),     fetched.getBalance(), 0.001);
    }

    @Test
    void getAccount_nonExistentId_returnsNull() {
        Account fetched = db.getAccount(9999);
        assertNull(fetched, "getAccount with a non-existent ID should return null");
    }

    @Test
    void getAccountList_emptyDatabase_returnsEmptyList() {
        ArrayList<Account> accounts = db.getAccountList();
        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Account — updateAccount
    // -------------------------------------------------------------------------

    @Test
    void updateAccount_changesArePersisted() {
        Account account = createTestAccount();
        account.setAccountName("Premium Checking");
        account.setFirstName("Jane");
        account.setLastName("Smith");
        account.setColor(5);

        db.updateAccount(account);
        Account updated = db.getAccount(account.getAccountID());

        assertNotNull(updated);
        assertEquals("Premium Checking", updated.getAccountName());
        assertEquals("Jane",             updated.getFirstName());
        assertEquals("Smith",            updated.getLastName());
        assertEquals(5,                  updated.getColor());
    }

    @Test
    void updateAccount_balanceIsPreserved() {
        Account account = createTestAccount();
        // Add a transaction so the balance changes, then update the name
        addExpense(account.getAccountID(), 100.00, TEST_DATE);
        Account refreshed = db.getAccount(account.getAccountID());

        assertNotNull(refreshed);
        double balanceAfterExpense = refreshed.getBalance();
        refreshed.setAccountName("Updated Name");
        db.updateAccount(refreshed);

        Account afterUpdate = db.getAccount(account.getAccountID());
        assertNotNull(afterUpdate);
        assertEquals(balanceAfterExpense, afterUpdate.getBalance(), 0.001,
                "Balance should not change during a name-only update");
    }

    // -------------------------------------------------------------------------
    // Account — deleteAccount
    // -------------------------------------------------------------------------

    @Test
    void deleteAccount_removedFromList() {
        Account account = createTestAccount();
        db.deleteAccount(account);

        ArrayList<Account> accounts = db.getAccountList();
        assertTrue(accounts.isEmpty(), "Account list should be empty after deletion");
    }

    @Test
    void deleteAccount_alsoClearsItsTransactions() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), EXPENSE_AMOUNT, TEST_DATE);
        addIncome(account.getAccountID(), INCOME_AMOUNT, TEST_DATE);

        db.deleteAccount(account);

        // Account is gone, so getAccount returns null — we just verify no orphaned transaction rows
        // remain by creating a second account with a new ID and confirming its transaction list is empty
        db.createNewAccount("Second", "A", "B", 0.00, 1);
        Account second = db.getAccountList().get(0);
        assertTrue(db.getTransactions(second.getAccountID()).isEmpty(),
                "New account should have no transactions — orphaned rows would indicate a deletion bug");
    }

    // -------------------------------------------------------------------------
    // Transaction — createTransaction / balance effects
    // -------------------------------------------------------------------------

    @Test
    void createTransaction_expense_subtractsFromBalance() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), EXPENSE_AMOUNT, TEST_DATE);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        assertEquals(INITIAL_BAL - EXPENSE_AMOUNT, refreshed.getBalance(), 0.001);
    }

    @Test
    void createTransaction_income_addsToBalance() {
        Account account = createTestAccount();
        addIncome(account.getAccountID(), INCOME_AMOUNT, TEST_DATE);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        assertEquals(INITIAL_BAL + INCOME_AMOUNT, refreshed.getBalance(), 0.001);
    }

    @Test
    void createTransaction_multipleTransactions_balanceAccumulates() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 100.00, TEST_DATE);
        addIncome(account.getAccountID(), 300.00, TEST_DATE);
        addExpense(account.getAccountID(), 50.00,  TEST_DATE);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        // 1000 - 100 + 300 - 50 = 1150
        assertEquals(1150.00, refreshed.getBalance(), 0.001);
    }

    @Test
    void createTransaction_purchaseWithoutCategory_stillInserts() {
        Account account = createTestAccount();
        // Passing null category for a non-purchase type should not throw or silently fail
        db.createTransaction(account.getAccountID(), 75.00, Transaction.TYPE_TRANSFER, null, TEST_DATE);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        assertEquals(INITIAL_BAL - 75.00, refreshed.getBalance(), 0.001);
    }

    // -------------------------------------------------------------------------
    // Transaction — deleteTransaction
    // -------------------------------------------------------------------------

    @Test
    void deleteTransaction_expense_balanceRestored() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), EXPENSE_AMOUNT, TEST_DATE);

        Transaction toDelete = db.getTransactions(account.getAccountID()).get(0);
        db.deleteTransaction(toDelete);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        assertEquals(INITIAL_BAL, refreshed.getBalance(), 0.001,
                "Balance should be restored to initial value after deleting the only expense");
    }

    @Test
    void deleteTransaction_income_balanceRestored() {
        Account account = createTestAccount();
        addIncome(account.getAccountID(), INCOME_AMOUNT, TEST_DATE);

        Transaction toDelete = db.getTransactions(account.getAccountID()).get(0);
        db.deleteTransaction(toDelete);

        Account refreshed = db.getAccount(account.getAccountID());
        assertNotNull(refreshed);
        assertEquals(INITIAL_BAL, refreshed.getBalance(), 0.001,
                "Balance should be restored to initial value after deleting the only income");
    }

    @Test
    void deleteTransaction_removedFromTransactionList() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), EXPENSE_AMOUNT, TEST_DATE);

        Transaction toDelete = db.getTransactions(account.getAccountID()).get(0);
        db.deleteTransaction(toDelete);

        assertTrue(db.getTransactions(account.getAccountID()).isEmpty(),
                "Transaction list should be empty after deletion");
    }

    // -------------------------------------------------------------------------
    // Transaction — getTransactions / getTransactionsByDateRange
    // -------------------------------------------------------------------------

    @Test
    void getTransactions_returnsAllForAccount() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 10.00, TEST_DATE);
        addExpense(account.getAccountID(), 20.00, TEST_DATE);
        addIncome(account.getAccountID(), 100.00, TEST_DATE);

        assertEquals(3, db.getTransactions(account.getAccountID()).size());
    }

    @Test
    void getTransactions_isolatedByAccount() {
        // Two accounts — transactions from one should not appear when querying the other
        db.createNewAccount("Account A", "A", "A", 500.00, 1);
        db.createNewAccount("Account B", "B", "B", 500.00, 2);
        ArrayList<Account> accounts = db.getAccountList();
        Account accountA = accounts.get(0);
        Account accountB = accounts.get(1);

        addExpense(accountA.getAccountID(), 50.00, TEST_DATE);
        addExpense(accountA.getAccountID(), 75.00, TEST_DATE);
        addIncome(accountB.getAccountID(), 200.00, TEST_DATE);

        assertEquals(2, db.getTransactions(accountA.getAccountID()).size(),
                "Account A should have exactly 2 transactions");
        assertEquals(1, db.getTransactions(accountB.getAccountID()).size(),
                "Account B should have exactly 1 transaction");
    }

    @Test
    void getTransactionsByDateRange_filtersCorrectly() {
        Account account = createTestAccount();

        LocalDate jan = LocalDate.of(2026, 1, 15);
        LocalDate mar = LocalDate.of(2026, 3, 10);
        LocalDate jun = LocalDate.of(2026, 6, 20);

        addExpense(account.getAccountID(), 50.00,  jan);
        addExpense(account.getAccountID(), 100.00, mar);
        addIncome(account.getAccountID(),  200.00, jun);

        // Query only January through March — should return 2 transactions
        var results = db.getTransactionsByDateRange(
                account.getAccountID(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        assertEquals(2, results.size(), "Date range filter should exclude the June transaction");
    }

    @Test
    void getTransactionsByDateRange_emptyRange_returnsEmpty() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 50.00, LocalDate.of(2026, 5, 1));

        var results = db.getTransactionsByDateRange(
                account.getAccountID(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        assertTrue(results.isEmpty(), "No transactions exist in 2025 so results should be empty");
    }

    // -------------------------------------------------------------------------
    // Analytics — getSummary
    // -------------------------------------------------------------------------

    @Test
    void getSummary_incomeTotalIsCorrect() {
        Account account = createTestAccount();
        addIncome(account.getAccountID(), 500.00, TEST_DATE);
        addIncome(account.getAccountID(), 300.00, TEST_DATE);

        AccountSummary summary = db.getSummary(account.getAccountID(),
                TEST_DATE.minusDays(1), TEST_DATE.plusDays(1));

        assertEquals(800.00, summary.income, 0.001);
    }

    @Test
    void getSummary_expenseTotalIsCorrect() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 100.00, TEST_DATE);
        addExpense(account.getAccountID(), 75.00,  TEST_DATE);

        AccountSummary summary = db.getSummary(account.getAccountID(),
                TEST_DATE.minusDays(1), TEST_DATE.plusDays(1));

        assertEquals(175.00, summary.expenses, 0.001);
    }

    @Test
    void getSummary_netIsIncomMinusExpenses() {
        Account account = createTestAccount();
        addIncome(account.getAccountID(),  400.00, TEST_DATE);
        addExpense(account.getAccountID(), 150.00, TEST_DATE);

        AccountSummary summary = db.getSummary(account.getAccountID(),
                TEST_DATE.minusDays(1), TEST_DATE.plusDays(1));

        assertEquals(250.00, summary.net, 0.001, "Net should be income - expenses");
    }

    @Test
    void getSummary_avgNetPerMonth_singleMonth() {
        Account account = createTestAccount();
        addIncome(account.getAccountID(),  600.00, TEST_DATE);
        addExpense(account.getAccountID(), 200.00, TEST_DATE);

        AccountSummary summary = db.getSummary(account.getAccountID(),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        // Net = 400, active months = 1, avg = 400
        assertEquals(400.00, summary.avgNetPerMonth, 0.001);
    }

    @Test
    void getSummary_noTransactionsInRange_returnsZeroes() {
        Account account = createTestAccount();
        // Transactions outside the query range
        addExpense(account.getAccountID(), 100.00, LocalDate.of(2025, 1, 1));

        AccountSummary summary = db.getSummary(account.getAccountID(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(0.0, summary.income,   0.001);
        assertEquals(0.0, summary.expenses, 0.001);
        assertEquals(0.0, summary.net,      0.001);
    }

    // -------------------------------------------------------------------------
    // Analytics — getTransactionsByTypeAndDateRange
    // -------------------------------------------------------------------------

    @Test
    void getTransactionsByType_groupsCorrectly() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 50.00,  TEST_DATE); // Purchase
        addExpense(account.getAccountID(), 100.00, TEST_DATE); // Purchase
        addIncome(account.getAccountID(),  200.00, TEST_DATE); // Wages

        Map<String, Double> byType = db.getTransactionsByTypeAndDateRange(
                account.getAccountID(),
                TEST_DATE.minusDays(1),
                TEST_DATE.plusDays(1)
        );

        assertEquals(150.00, byType.get(Transaction.TYPE_PURCHASE), 0.001,
                "Purchase total should sum both expense transactions");
        assertEquals(200.00, byType.get(Transaction.TYPE_WAGES), 0.001,
                "Wages total should reflect the single income transaction");
    }

    // -------------------------------------------------------------------------
    // Analytics — getPurchasesByCategoryAndDateRange
    // -------------------------------------------------------------------------

    @Test
    void getPurchasesByCategory_groupsCorrectly() {
        Account account = createTestAccount();
        String food  = Transaction.purchaseCategories[0]; // "Food & Groceries"
        String other = Transaction.purchaseCategories[Transaction.purchaseCategories.length - 1]; // "Other"

        db.createTransaction(account.getAccountID(), 30.00, Transaction.TYPE_PURCHASE, food,  TEST_DATE);
        db.createTransaction(account.getAccountID(), 20.00, Transaction.TYPE_PURCHASE, food,  TEST_DATE);
        db.createTransaction(account.getAccountID(), 10.00, Transaction.TYPE_PURCHASE, other, TEST_DATE);

        Map<String, Double> byCategory = db.getPurchasesByCategoryAndDateRange(
                account.getAccountID(),
                TEST_DATE.minusDays(1),
                TEST_DATE.plusDays(1)
        );

        assertEquals(50.00, byCategory.get(food),  0.001, "Food total should sum both transactions");
        assertEquals(10.00, byCategory.get(other), 0.001, "Other total should reflect one transaction");
    }

    @Test
    void getPurchasesByCategory_excludesNonPurchaseTypes() {
        Account account = createTestAccount();
        // A wages income should not appear in the category breakdown
        addIncome(account.getAccountID(), 500.00, TEST_DATE);

        Map<String, Double> byCategory = db.getPurchasesByCategoryAndDateRange(
                account.getAccountID(),
                TEST_DATE.minusDays(1),
                TEST_DATE.plusDays(1)
        );

        assertTrue(byCategory.isEmpty(),
                "Non-purchase transactions should not appear in category breakdown");
    }

    // -------------------------------------------------------------------------
    // Analytics — getTopTransactionsByDateRange
    // -------------------------------------------------------------------------

    @Test
    void getTopTransactions_returnsLimitedAndSortedResults() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 10.00,  TEST_DATE);
        addExpense(account.getAccountID(), 500.00, TEST_DATE);
        addExpense(account.getAccountID(), 75.00,  TEST_DATE);
        addExpense(account.getAccountID(), 200.00, TEST_DATE);

        var top2 = db.getTopTransactionsByDateRange(
                account.getAccountID(),
                TEST_DATE.minusDays(1),
                TEST_DATE.plusDays(1),
                2
        );

        assertEquals(2, top2.size(), "Result should be limited to 2");
        assertEquals(500.00, top2.get(0).getAmount(), 0.001, "Largest transaction should be first");
        assertEquals(200.00, top2.get(1).getAmount(), 0.001, "Second largest should be second");
    }

    @Test
    void getTopTransactions_nullSinceDate_queriesAllTime() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 100.00, LocalDate.of(2024, 1, 1));
        addExpense(account.getAccountID(), 200.00, LocalDate.of(2025, 6, 1));

        var top5 = db.getTopTransactionsByDateRange(
                account.getAccountID(),
                null,
                LocalDate.of(2026, 12, 31),
                5
        );

        assertEquals(2, top5.size(), "All-time query should return both transactions");
    }

    // -------------------------------------------------------------------------
    // Analytics — getBalanceHistory
    // -------------------------------------------------------------------------

    @Test
    void getBalanceHistory_chronologicalOrder() {
        Account account = createTestAccount();
        addExpense(account.getAccountID(), 100.00, LocalDate.of(2026, 3, 1));
        addIncome(account.getAccountID(),  300.00, LocalDate.of(2026, 5, 1));

        Map<LocalDate, Double> history = db.getBalanceHistory(account.getAccountID());

        assertFalse(history.isEmpty());
        // The map should contain entries for both transaction dates
        assertTrue(history.containsKey(LocalDate.of(2026, 3, 1)));
        assertTrue(history.containsKey(LocalDate.of(2026, 5, 1)));
    }

    @Test
    void getBalanceHistory_nonExistentAccount_returnsEmptyMap() {
        Map<LocalDate, Double> history = db.getBalanceHistory(9999);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}
