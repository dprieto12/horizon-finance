package models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the three model classes — Account, Transaction, and AccountSummary.
 * No database or JavaFX context is needed since these classes are pure Java.
 *
 * Placement: src/test/java/models/ModelsTest.java
 */
class ModelsTest {

    // =========================================================================
    // Account
    // =========================================================================

    // -------------------------------------------------------------------------
    // Constructor and getters
    // -------------------------------------------------------------------------

    @Test
    void account_fullConstructor_fieldsAssignedCorrectly() {
        Account a = new Account(1, "Checking", "John", "Doe", 1000.00, 3);

        assertEquals(1,          a.getAccountID());
        assertEquals("Checking", a.getAccountName());
        assertEquals("John",     a.getFirstName());
        assertEquals("Doe",      a.getLastName());
        assertEquals(1000.00,    a.getBalance(), 0.001);
        assertEquals(3,          a.getColor());
    }

    @Test
    void account_defaultColorConstructor_usesDefaultColor() {
        Account a = new Account(1, "Savings", "Jane", "Smith", 500.00);
        assertEquals(Account.DEFAULT_COLOR, a.getColor());
    }

    // -------------------------------------------------------------------------
    // setColor — bounds validation
    // -------------------------------------------------------------------------

    @Test
    void account_setColor_validIndex_stored() {
        Account a = new Account(1, "Checking", "John", "Doe", 0.00);
        a.setColor(Account.COLOR_COUNT); // highest valid index
        assertEquals(Account.COLOR_COUNT, a.getColor());
    }

    @Test
    void account_setColor_zero_fallsBackToDefault() {
        Account a = new Account(1, "Checking", "John", "Doe", 0.00);
        a.setColor(0);
        assertEquals(Account.DEFAULT_COLOR, a.getColor(),
                "Color index 0 is out of range and should fall back to DEFAULT_COLOR");
    }

    @Test
    void account_setColor_negative_fallsBackToDefault() {
        Account a = new Account(1, "Checking", "John", "Doe", 0.00);
        a.setColor(-5);
        assertEquals(Account.DEFAULT_COLOR, a.getColor(),
                "Negative color index should fall back to DEFAULT_COLOR");
    }

    @Test
    void account_setColor_aboveMax_fallsBackToDefault() {
        Account a = new Account(1, "Checking", "John", "Doe", 0.00);
        a.setColor(Account.COLOR_COUNT + 1);
        assertEquals(Account.DEFAULT_COLOR, a.getColor(),
                "Color index above COLOR_COUNT should fall back to DEFAULT_COLOR");
    }

    @Test
    void account_constructorColorOutOfRange_clampsToDefault() {
        // Passing an out-of-range color directly to the constructor should also clamp
        Account a = new Account(1, "Checking", "John", "Doe", 0.00, 999);
        assertEquals(Account.DEFAULT_COLOR, a.getColor());
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    @Test
    void account_setAccountName_updatesCorrectly() {
        Account a = new Account(1, "Old Name", "John", "Doe", 0.00);
        a.setAccountName("New Name");
        assertEquals("New Name", a.getAccountName());
    }

    @Test
    void account_setFirstName_updatesCorrectly() {
        Account a = new Account(1, "Checking", "Old", "Doe", 0.00);
        a.setFirstName("Jane");
        assertEquals("Jane", a.getFirstName());
    }

    @Test
    void account_setLastName_updatesCorrectly() {
        Account a = new Account(1, "Checking", "John", "Old", 0.00);
        a.setLastName("Smith");
        assertEquals("Smith", a.getLastName());
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Test
    void account_toString_containsExpectedFields() {
        Account a = new Account(22, "Student Checking", "John", "Doe", 1000.00, 1);
        String result = a.toString();

        assertTrue(result.contains("Student Checking"));
        assertTrue(result.contains("ID#22"));
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
        assertTrue(result.contains("1000.00"));
    }

    // =========================================================================
    // Transaction
    // =========================================================================

    private static final LocalDate DATE = LocalDate.of(2026, 5, 15);

    // -------------------------------------------------------------------------
    // isExpense
    // -------------------------------------------------------------------------

    @Test
    void transaction_isExpense_purchaseType_returnsTrue() {
        assertTrue(Transaction.isExpense(Transaction.TYPE_PURCHASE));
    }

    @Test
    void transaction_isExpense_transferType_returnsTrue() {
        assertTrue(Transaction.isExpense(Transaction.TYPE_TRANSFER));
    }

    @Test
    void transaction_isExpense_withdrawalType_returnsTrue() {
        assertTrue(Transaction.isExpense(Transaction.TYPE_WITHDRAWAL));
    }

    @Test
    void transaction_isExpense_billType_returnsTrue() {
        assertTrue(Transaction.isExpense(Transaction.TYPE_BILL));
    }

    @Test
    void transaction_isExpense_feeType_returnsTrue() {
        assertTrue(Transaction.isExpense(Transaction.TYPE_FEE));
    }

    @Test
    void transaction_isExpense_wagesType_returnsFalse() {
        assertFalse(Transaction.isExpense(Transaction.TYPE_WAGES));
    }

    @Test
    void transaction_isExpense_saleType_returnsFalse() {
        assertFalse(Transaction.isExpense(Transaction.TYPE_SALE));
    }

    @Test
    void transaction_isExpense_giftType_returnsFalse() {
        assertFalse(Transaction.isExpense(Transaction.TYPE_GIFT));
    }

    @Test
    void transaction_isExpense_refundType_returnsFalse() {
        assertFalse(Transaction.isExpense(Transaction.TYPE_REFUND));
    }

    @Test
    void transaction_isExpense_interestType_returnsFalse() {
        assertFalse(Transaction.isExpense(Transaction.TYPE_INTEREST));
    }

    @Test
    void transaction_isExpense_caseInsensitive() {
        // isExpense uses equalsIgnoreCase, so mixed case should still match
        assertTrue(Transaction.isExpense("purchase"),
                "isExpense should be case-insensitive");
        assertTrue(Transaction.isExpense("TRANSFER"),
                "isExpense should be case-insensitive");
        assertFalse(Transaction.isExpense("WAGES"),
                "Income types should return false regardless of case");
    }

    // -------------------------------------------------------------------------
    // requiresCategory
    // -------------------------------------------------------------------------

    @Test
    void transaction_requiresCategory_purchaseType_returnsTrue() {
        assertTrue(Transaction.requiresCategory(Transaction.TYPE_PURCHASE));
    }

    @Test
    void transaction_requiresCategory_nonPurchaseTypes_returnFalse() {
        assertFalse(Transaction.requiresCategory(Transaction.TYPE_TRANSFER));
        assertFalse(Transaction.requiresCategory(Transaction.TYPE_WAGES));
        assertFalse(Transaction.requiresCategory(Transaction.TYPE_GIFT));
        assertFalse(Transaction.requiresCategory(Transaction.TYPE_BILL));
        assertFalse(Transaction.requiresCategory(Transaction.TYPE_FEE));
    }

    @Test
    void transaction_requiresCategory_caseInsensitive() {
        assertTrue(Transaction.requiresCategory("purchase"),
                "requiresCategory should be case-insensitive");
    }

    // -------------------------------------------------------------------------
    // Constructor and getters
    // -------------------------------------------------------------------------

    @Test
    void transaction_fullConstructor_fieldsAssignedCorrectly() {
        Transaction t = new Transaction(4, 1, 25.00, Transaction.TYPE_PURCHASE,
                "Food & Groceries", DATE);

        assertEquals(4,                   t.getTransactionID());
        assertEquals(1,                   t.getAccountID());
        assertEquals(25.00,               t.getAmount(), 0.001);
        assertEquals(Transaction.TYPE_PURCHASE, t.getType());
        assertEquals("Food & Groceries",  t.getCategory());
        assertEquals(DATE,                t.getDate());
    }

    @Test
    void transaction_noDateConstructor_defaultsToToday() {
        Transaction t = new Transaction(1, 50.00, Transaction.TYPE_WAGES, null);
        assertEquals(LocalDate.now(), t.getDate());
    }

    @Test
    void transaction_nonPurchase_categoryIsNull() {
        Transaction t = new Transaction(1, 1, 100.00, Transaction.TYPE_WAGES, null, DATE);
        assertNull(t.getCategory(),
                "Non-purchase transactions should have a null category");
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Test
    void transaction_toString_purchase_includesCategory() {
        Transaction t = new Transaction(4, 1, 25.00, Transaction.TYPE_PURCHASE,
                "Food & Groceries", DATE);
        String result = t.toString();

        assertTrue(result.contains("TID#4"));
        assertTrue(result.contains("25.00"));
        assertTrue(result.contains("Purchase"));
        assertTrue(result.contains("Food & Groceries"));
        assertTrue(result.contains("2026-05-15"));
    }

    @Test
    void transaction_toString_nonPurchase_omitsCategory() {
        Transaction t = new Transaction(5, 1, 500.00, Transaction.TYPE_WAGES, null, DATE);
        String result = t.toString();

        assertTrue(result.contains("TID#5"));
        assertTrue(result.contains("500.00"));
        assertTrue(result.contains("Wages"));
        // Category section should not appear for non-purchases
        assertFalse(result.contains("null"),
                "toString should not print the word 'null' for missing category");
    }

    // -------------------------------------------------------------------------
    // Static arrays completeness
    // -------------------------------------------------------------------------

    @Test
    void transaction_expenseTypesArray_isNotEmpty() {
        assertNotNull(Transaction.expenseTypes);
        assertTrue(Transaction.expenseTypes.length > 0);
    }

    @Test
    void transaction_incomeTypesArray_isNotEmpty() {
        assertNotNull(Transaction.incomeTypes);
        assertTrue(Transaction.incomeTypes.length > 0);
    }

    @Test
    void transaction_purchaseCategoriesArray_isNotEmpty() {
        assertNotNull(Transaction.purchaseCategories);
        assertTrue(Transaction.purchaseCategories.length > 0);
    }

    @Test
    void transaction_allExpenseTypesReturnTrueForIsExpense() {
        for (String type : Transaction.expenseTypes) {
            assertTrue(Transaction.isExpense(type),
                    "Every type in expenseTypes should return true for isExpense: " + type);
        }
    }

    @Test
    void transaction_allIncomeTypesReturnFalseForIsExpense() {
        for (String type : Transaction.incomeTypes) {
            assertFalse(Transaction.isExpense(type),
                    "Every type in incomeTypes should return false for isExpense: " + type);
        }
    }

    // =========================================================================
    // AccountSummary
    // =========================================================================

    @Test
    void accountSummary_fieldsAssignedCorrectly() {
        AccountSummary s = new AccountSummary(1000.00, 600.00, 400.00, 200.00);

        assertEquals(1000.00, s.income,          0.001);
        assertEquals(600.00,  s.expenses,        0.001);
        assertEquals(400.00,  s.net,             0.001);
        assertEquals(200.00,  s.avgNetPerMonth,  0.001);
    }

    @Test
    void accountSummary_toString_positiveNet_includesPlusSign() {
        AccountSummary s = new AccountSummary(500.00, 200.00, 300.00, 150.00);
        String result = s.toString();

        assertTrue(result.contains("+$300.00"),
                "Positive net should display with a + prefix");
        assertTrue(result.contains("+$150.00"),
                "Positive avgNetPerMonth should display with a + prefix");
    }

    @Test
    void accountSummary_toString_negativeNet_includesMinusSign() {
        AccountSummary s = new AccountSummary(100.00, 400.00, -300.00, -150.00);
        String result = s.toString();

        assertTrue(result.contains("-$300.00"),
                "Negative net should display with a - prefix");
        assertTrue(result.contains("-$150.00"),
                "Negative avgNetPerMonth should display with a - prefix");
    }

    @Test
    void accountSummary_toString_containsAllFourValues() {
        AccountSummary s = new AccountSummary(800.00, 350.00, 450.00, 225.00);
        String result = s.toString();

        assertTrue(result.contains("800.00"));
        assertTrue(result.contains("350.00"));
        assertTrue(result.contains("450.00"));
        assertTrue(result.contains("225.00"));
    }

    @Test
    void accountSummary_zeroValues_doesNotThrow() {
        AccountSummary s = new AccountSummary(0.00, 0.00, 0.00, 0.00);
        assertDoesNotThrow(s::toString);
    }
}
