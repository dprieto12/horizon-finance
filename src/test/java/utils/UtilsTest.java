package utils;

import javafx.scene.control.TextField;
import models.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TextFieldUtils and ApplicationState.
 *
 * TextFieldUtils.fieldsAreFilled() takes TextField objects, which are JavaFX controls. JavaFX controls
 * require the JavaFX toolkit to be initialised before they can be instantiated — even in tests that
 * never show a window. The @BeforeAll block handles this by starting the toolkit once per test class
 * via Platform.startup(), which is a no-op if it is already running.
 *
 * Placement: src/test/java/utils/UtilsTest.java
 */
class UtilsTest {

    // -------------------------------------------------------------------------
    // JavaFX toolkit initialisation
    // -------------------------------------------------------------------------

    @BeforeAll
    static void initToolkit() {
        try {
            // Start the JavaFX toolkit without opening a window.
            // If the toolkit is already running (e.g. another test class started it first),
            // this call is silently ignored.
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already started — safe to continue
        }
    }

    // =========================================================================
    // TextFieldUtils — fieldsAreFilled
    // =========================================================================

    private TextField filledField;
    private TextField emptyField;
    private TextField whitespaceField;

    @BeforeEach
    void setUpFields() {
        filledField     = new TextField("some text");
        emptyField      = new TextField("");
        whitespaceField = new TextField("   ");
    }

    @Test
    void fieldsAreFilled_allFilled_returnsTrue() {
        TextField a = new TextField("John");
        TextField b = new TextField("Doe");
        TextField c = new TextField("Checking");

        assertTrue(TextFieldUtils.fieldsAreFilled(new TextField[]{a, b, c}));
    }

    @Test
    void fieldsAreFilled_oneEmpty_returnsFalse() {
        assertTrue(!TextFieldUtils.fieldsAreFilled(
                new TextField[]{filledField, emptyField, filledField}));
    }

    @Test
    void fieldsAreFilled_allEmpty_returnsFalse() {
        assertFalse(TextFieldUtils.fieldsAreFilled(
                new TextField[]{emptyField, emptyField}));
    }

    @Test
    void fieldsAreFilled_whitespaceOnly_returnsFalse() {
        // trim() is applied inside fieldsAreFilled, so whitespace-only counts as empty
        assertFalse(TextFieldUtils.fieldsAreFilled(new TextField[]{whitespaceField}),
                "A field containing only whitespace should be treated as empty");
    }

    @Test
    void fieldsAreFilled_singleFilledField_returnsTrue() {
        assertTrue(TextFieldUtils.fieldsAreFilled(new TextField[]{filledField}));
    }

    @Test
    void fieldsAreFilled_emptyArray_returnsTrue() {
        // No fields to check — vacuously true, consistent with how the loop exits
        assertTrue(TextFieldUtils.fieldsAreFilled(new TextField[]{}),
                "An empty array has no unfilled fields, so the result should be true");
    }

    @Test
    void fieldsAreFilled_lastFieldEmpty_returnsFalse() {
        // Ensure the check doesn't short-circuit too early and miss the last field
        assertFalse(TextFieldUtils.fieldsAreFilled(
                new TextField[]{filledField, filledField, emptyField}));
    }

    // =========================================================================
    // TextFieldUtils — createLengthLimitFormatter
    // =========================================================================

    @Test
    void lengthLimitFormatter_withinLimit_allowsInput() {
        TextField field = new TextField();
        field.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(10));
        field.setText("Hello");

        // If the formatter allows it, the text is set
        assertEquals("Hello", field.getText());
    }

    @Test
    void lengthLimitFormatter_exactlyAtLimit_allowed() {
        int limit = 5;
        TextField field = new TextField();
        field.setTextFormatter(TextFieldUtils.createLengthLimitFormatter(limit));
        field.setText("12345"); // exactly 5 characters

        assertEquals("12345", field.getText());
    }

    // =========================================================================
    // TextFieldUtils — createNumberLimitFormatter
    // =========================================================================

    @Test
    void numberLimitFormatter_numericInput_withinLimit_allowed() {
        TextField field = new TextField();
        field.setTextFormatter(TextFieldUtils.createNumberLimitFormatter(9));
        field.setText("12345");

        assertEquals("12345", field.getText());
    }

    @Test
    void numberLimitFormatter_emptyString_allowed() {
        // Clearing the field (empty string) should be allowed — the formatter permits isEmpty()
        TextField field = new TextField();
        field.setTextFormatter(TextFieldUtils.createNumberLimitFormatter(9));
        field.setText("123");
        field.clear();

        assertEquals("", field.getText());
    }

    // =========================================================================
    // ApplicationState
    // =========================================================================

    @BeforeEach
    void resetApplicationState() {
        // Reset to null before each test so tests don't bleed into each other
        ApplicationState.setCurrentAccount(null);
    }

    @Test
    void applicationState_setAndGet_returnsCorrectAccount() {
        Account account = new Account(1, "Checking", "John", "Doe", 500.00, 2);
        ApplicationState.setCurrentAccount(account);

        Account retrieved = ApplicationState.getCurrentAccount();
        assertNotNull(retrieved);
        assertEquals(1,          retrieved.getAccountID());
        assertEquals("Checking", retrieved.getAccountName());
    }

    @Test
    void applicationState_setNull_returnsNull() {
        ApplicationState.setCurrentAccount(new Account(1, "A", "B", "C", 0.00));
        ApplicationState.setCurrentAccount(null);

        assertNull(ApplicationState.getCurrentAccount(),
                "Setting current account to null should clear it");
    }

    @Test
    void applicationState_overwrite_returnsLatestAccount() {
        Account first  = new Account(1, "Savings",  "Jane", "Doe", 1000.00, 1);
        Account second = new Account(2, "Checking", "John", "Doe", 500.00,  3);

        ApplicationState.setCurrentAccount(first);
        ApplicationState.setCurrentAccount(second);

        Account retrieved = ApplicationState.getCurrentAccount();
        assertNotNull(retrieved);
        assertEquals(2,          retrieved.getAccountID(),
                "ApplicationState should reflect the most recently set account");
        assertEquals("Checking", retrieved.getAccountName());
    }

    @Test
    void applicationState_initialState_isNull() {
        // After resetApplicationState() sets null, confirm the initial state is null
        assertNull(ApplicationState.getCurrentAccount(),
                "ApplicationState should start null before any account is set");
    }
}
