package database;

import models.Transaction;

import java.time.LocalDate;

/**
 * Utility class to generate test data for development and testing.
 * Uses DatabaseManager methods to ensure data follows application norms.
 */
public class TestDataGenerator {

    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        
        // Create 3 test accounts with different colors
        int account1 = createAccount(db, "Personal Checking", "John", "Doe", 5000.00, 1);
        int account2 = createAccount(db, "Business Savings", "Jane", "Smith", 15000.00, 2);
        int account3 = createAccount(db, "Student Account", "Alex", "Johnson", 1200.00, 3);
        
        System.out.println("Created 3 accounts with IDs: " + account1 + ", " + account2 + ", " + account3);
        
        // Generate transactions for account 1 (Personal Checking - 6 months of data)
        generatePersonalAccountTransactions(db, account1);
        
        // Generate transactions for account 2 (Business Savings - 6 months of data)
        generateBusinessAccountTransactions(db, account2);
        
        // Generate transactions for account 3 (Student Account - 6 months of data)
        generateStudentAccountTransactions(db, account3);
        
        System.out.println("Test data generation complete!");
    }
    
    private static int createAccount(DatabaseManager db, String accountName, String firstName, String lastName, double balance, int color) {
        db.createNewAccount(accountName, firstName, lastName, balance, color);
        // Get the account ID by fetching the most recently created account
        var accounts = db.getAccountList();
        return accounts.get(accounts.size() - 1).getAccountID();
    }
    
    private static void generatePersonalAccountTransactions(DatabaseManager db, int accountID) {
        LocalDate baseDate = LocalDate.now().minusMonths(6);
        
        // Monthly income
        db.createTransaction(accountID, 4500.00, Transaction.TYPE_WAGES, null, baseDate.plusDays(1));
        db.createTransaction(accountID, 4500.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(1).plusDays(1));
        db.createTransaction(accountID, 4600.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(2).plusDays(1));
        db.createTransaction(accountID, 4500.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(3).plusDays(1));
        db.createTransaction(accountID, 4700.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(4).plusDays(1));
        db.createTransaction(accountID, 4800.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(5).plusDays(1));
        
        // Regular purchases - Food & Groceries
        db.createTransaction(accountID, 150.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(3));
        db.createTransaction(accountID, 180.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(10));
        db.createTransaction(accountID, 165.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(17));
        db.createTransaction(accountID, 200.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(24));
        db.createTransaction(accountID, 145.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(1).plusDays(3));
        db.createTransaction(accountID, 190.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(1).plusDays(10));
        db.createTransaction(accountID, 175.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(2).plusDays(3));
        db.createTransaction(accountID, 185.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(3).plusDays(3));
        db.createTransaction(accountID, 160.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(4).plusDays(3));
        db.createTransaction(accountID, 195.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(5).plusDays(3));
        
        // Housing
        db.createTransaction(accountID, 1200.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusDays(5));
        db.createTransaction(accountID, 1200.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusMonths(1).plusDays(5));
        db.createTransaction(accountID, 1200.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusMonths(2).plusDays(5));
        db.createTransaction(accountID, 1200.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusMonths(3).plusDays(5));
        db.createTransaction(accountID, 1250.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusMonths(4).plusDays(5));
        db.createTransaction(accountID, 1250.00, Transaction.TYPE_PURCHASE, "Housing", baseDate.plusMonths(5).plusDays(5));
        
        // Transportation
        db.createTransaction(accountID, 80.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusDays(8));
        db.createTransaction(accountID, 75.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusDays(22));
        db.createTransaction(accountID, 85.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(1).plusDays(8));
        db.createTransaction(accountID, 90.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(2).plusDays(8));
        db.createTransaction(accountID, 78.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(3).plusDays(8));
        db.createTransaction(accountID, 82.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(4).plusDays(8));
        
        // Entertainment
        db.createTransaction(accountID, 120.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusDays(12));
        db.createTransaction(accountID, 65.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusDays(28));
        db.createTransaction(accountID, 150.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(1).plusDays(15));
        db.createTransaction(accountID, 95.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(2).plusDays(12));
        db.createTransaction(accountID, 110.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(3).plusDays(20));
        db.createTransaction(accountID, 140.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(4).plusDays(18));
        
        // Healthcare
        db.createTransaction(accountID, 50.00, Transaction.TYPE_PURCHASE, "Healthcare", baseDate.plusDays(15));
        db.createTransaction(accountID, 200.00, Transaction.TYPE_PURCHASE, "Healthcare", baseDate.plusMonths(2).plusDays(10));
        db.createTransaction(accountID, 75.00, Transaction.TYPE_PURCHASE, "Healthcare", baseDate.plusMonths(4).plusDays(25));
        
        // Bills
        db.createTransaction(accountID, 150.00, Transaction.TYPE_BILL, null, baseDate.plusDays(7));
        db.createTransaction(accountID, 150.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(1).plusDays(7));
        db.createTransaction(accountID, 155.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(2).plusDays(7));
        db.createTransaction(accountID, 155.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(3).plusDays(7));
        db.createTransaction(accountID, 160.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(4).plusDays(7));
        db.createTransaction(accountID, 160.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(5).plusDays(7));
        
        // Transfer to savings
        db.createTransaction(accountID, 500.00, Transaction.TYPE_TRANSFER, null, baseDate.plusDays(30));
        db.createTransaction(accountID, 500.00, Transaction.TYPE_TRANSFER, null, baseDate.plusMonths(1).plusDays(30));
        db.createTransaction(accountID, 600.00, Transaction.TYPE_TRANSFER, null, baseDate.plusMonths(2).plusDays(30));
        db.createTransaction(accountID, 550.00, Transaction.TYPE_TRANSFER, null, baseDate.plusMonths(3).plusDays(30));
        db.createTransaction(accountID, 700.00, Transaction.TYPE_TRANSFER, null, baseDate.plusMonths(4).plusDays(30));
        
        // One-time income
        db.createTransaction(accountID, 500.00, Transaction.TYPE_SALE, null, baseDate.plusDays(20));
        db.createTransaction(accountID, 200.00, Transaction.TYPE_GIFT, null, baseDate.plusMonths(2).plusDays(15));
        db.createTransaction(accountID, 50.00, Transaction.TYPE_REFUND, null, baseDate.plusMonths(4).plusDays(5));
        
        // Fees
        db.createTransaction(accountID, 5.00, Transaction.TYPE_FEE, null, baseDate.plusDays(2));
        db.createTransaction(accountID, 5.00, Transaction.TYPE_FEE, null, baseDate.plusMonths(2).plusDays(2));
        db.createTransaction(accountID, 8.00, Transaction.TYPE_FEE, null, baseDate.plusMonths(4).plusDays(2));
        
        System.out.println("Generated ~50 transactions for Personal Checking account");
    }
    
    private static void generateBusinessAccountTransactions(DatabaseManager db, int accountID) {
        LocalDate baseDate = LocalDate.now().minusMonths(6);
        
        // Business income - larger amounts
        db.createTransaction(accountID, 8000.00, Transaction.TYPE_WAGES, null, baseDate.plusDays(1));
        db.createTransaction(accountID, 8200.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(1).plusDays(1));
        db.createTransaction(accountID, 7800.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(2).plusDays(1));
        db.createTransaction(accountID, 8500.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(3).plusDays(1));
        db.createTransaction(accountID, 9000.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(4).plusDays(1));
        db.createTransaction(accountID, 8800.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(5).plusDays(1));
        
        // Business sales
        db.createTransaction(accountID, 2500.00, Transaction.TYPE_SALE, null, baseDate.plusDays(15));
        db.createTransaction(accountID, 3200.00, Transaction.TYPE_SALE, null, baseDate.plusMonths(1).plusDays(20));
        db.createTransaction(accountID, 1800.00, Transaction.TYPE_SALE, null, baseDate.plusMonths(2).plusDays(10));
        db.createTransaction(accountID, 4100.00, Transaction.TYPE_SALE, null, baseDate.plusMonths(3).plusDays(25));
        db.createTransaction(accountID, 2900.00, Transaction.TYPE_SALE, null, baseDate.plusMonths(4).plusDays(15));
        
        // Business expenses
        db.createTransaction(accountID, 500.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusDays(5));
        db.createTransaction(accountID, 750.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusDays(18));
        db.createTransaction(accountID, 600.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusMonths(1).plusDays(12));
        db.createTransaction(accountID, 900.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusMonths(2).plusDays(8));
        db.createTransaction(accountID, 450.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusMonths(3).plusDays(22));
        db.createTransaction(accountID, 800.00, Transaction.TYPE_PURCHASE, "Other", baseDate.plusMonths(4).plusDays(14));
        
        // Business bills
        db.createTransaction(accountID, 400.00, Transaction.TYPE_BILL, null, baseDate.plusDays(10));
        db.createTransaction(accountID, 400.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(1).plusDays(10));
        db.createTransaction(accountID, 420.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(2).plusDays(10));
        db.createTransaction(accountID, 420.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(3).plusDays(10));
        db.createTransaction(accountID, 450.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(4).plusDays(10));
        db.createTransaction(accountID, 450.00, Transaction.TYPE_BILL, null, baseDate.plusMonths(5).plusDays(10));
        
        // Interest income
        db.createTransaction(accountID, 25.00, Transaction.TYPE_INTEREST, null, baseDate.plusMonths(1));
        db.createTransaction(accountID, 28.00, Transaction.TYPE_INTEREST, null, baseDate.plusMonths(3));
        db.createTransaction(accountID, 32.00, Transaction.TYPE_INTEREST, null, baseDate.plusMonths(5));
        
        System.out.println("Generated ~25 transactions for Business Savings account");
    }
    
    private static void generateStudentAccountTransactions(DatabaseManager db, int accountID) {
        LocalDate baseDate = LocalDate.now().minusMonths(6);
        
        // Part-time job income
        db.createTransaction(accountID, 800.00, Transaction.TYPE_WAGES, null, baseDate.plusDays(5));
        db.createTransaction(accountID, 800.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(1).plusDays(5));
        db.createTransaction(accountID, 750.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(2).plusDays(5));
        db.createTransaction(accountID, 850.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(3).plusDays(5));
        db.createTransaction(accountID, 900.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(4).plusDays(5));
        db.createTransaction(accountID, 880.00, Transaction.TYPE_WAGES, null, baseDate.plusMonths(5).plusDays(5));
        
        // School expenses
        db.createTransaction(accountID, 300.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusDays(2));
        db.createTransaction(accountID, 150.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusDays(15));
        db.createTransaction(accountID, 200.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusMonths(1).plusDays(10));
        db.createTransaction(accountID, 400.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusMonths(2).plusDays(5));
        db.createTransaction(accountID, 180.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusMonths(3).plusDays(12));
        db.createTransaction(accountID, 250.00, Transaction.TYPE_PURCHASE, "School", baseDate.plusMonths(4).plusDays(8));
        
        // Food & Groceries (student budget)
        db.createTransaction(accountID, 80.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(7));
        db.createTransaction(accountID, 75.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(14));
        db.createTransaction(accountID, 90.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(21));
        db.createTransaction(accountID, 85.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusDays(28));
        db.createTransaction(accountID, 78.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(1).plusDays(7));
        db.createTransaction(accountID, 82.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(1).plusDays(14));
        db.createTransaction(accountID, 88.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(2).plusDays(7));
        db.createTransaction(accountID, 92.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(3).plusDays(7));
        db.createTransaction(accountID, 76.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(4).plusDays(7));
        db.createTransaction(accountID, 84.00, Transaction.TYPE_PURCHASE, "Food & Groceries", baseDate.plusMonths(5).plusDays(7));
        
        // Entertainment (student)
        db.createTransaction(accountID, 40.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusDays(10));
        db.createTransaction(accountID, 35.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusDays(25));
        db.createTransaction(accountID, 50.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(1).plusDays(20));
        db.createTransaction(accountID, 45.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(2).plusDays(15));
        db.createTransaction(accountID, 55.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(3).plusDays(22));
        db.createTransaction(accountID, 38.00, Transaction.TYPE_PURCHASE, "Entertainment", baseDate.plusMonths(4).plusDays(18));
        
        // Transportation (bus/student)
        db.createTransaction(accountID, 50.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusDays(3));
        db.createTransaction(accountID, 50.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(1).plusDays(3));
        db.createTransaction(accountID, 55.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(2).plusDays(3));
        db.createTransaction(accountID, 55.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(3).plusDays(3));
        db.createTransaction(accountID, 60.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(4).plusDays(3));
        db.createTransaction(accountID, 60.00, Transaction.TYPE_PURCHASE, "Transportation", baseDate.plusMonths(5).plusDays(3));
        
        // Personal
        db.createTransaction(accountID, 30.00, Transaction.TYPE_PURCHASE, "Personal", baseDate.plusDays(12));
        db.createTransaction(accountID, 45.00, Transaction.TYPE_PURCHASE, "Personal", baseDate.plusMonths(1).plusDays(18));
        db.createTransaction(accountID, 25.00, Transaction.TYPE_PURCHASE, "Personal", baseDate.plusMonths(2).plusDays(25));
        db.createTransaction(accountID, 40.00, Transaction.TYPE_PURCHASE, "Personal", baseDate.plusMonths(3).plusDays(8));
        db.createTransaction(accountID, 35.00, Transaction.TYPE_PURCHASE, "Personal", baseDate.plusMonths(4).plusDays(15));
        
        // Gift from family
        db.createTransaction(accountID, 200.00, Transaction.TYPE_GIFT, null, baseDate.plusDays(1));
        db.createTransaction(accountID, 150.00, Transaction.TYPE_GIFT, null, baseDate.plusMonths(3).plusDays(1));
        
        // Withdrawals
        db.createTransaction(accountID, 100.00, Transaction.TYPE_WITHDRAWAL, null, baseDate.plusDays(20));
        db.createTransaction(accountID, 100.00, Transaction.TYPE_WITHDRAWAL, null, baseDate.plusMonths(2).plusDays(20));
        db.createTransaction(accountID, 150.00, Transaction.TYPE_WITHDRAWAL, null, baseDate.plusMonths(4).plusDays(20));
        
        System.out.println("Generated ~35 transactions for Student account");
    }
}
