package models;

/**
 * <p>Class representing Accounts, holding only account info and no transaction data. Used by Controllers and
 * DatabaseManager in order to communicate data between the client and the database. DatabaseManager converts SQL data
 * stored in the accounts table to Account objects and uses altered Account objects to update the SQL database.</p>
 *
 * <p>Along with Transaction and AccountSummary, Account is one of the three models used by the application. It is
 * only used to represent SQL Database information, assisting in passing that information back and forth but never
 * being the key source for that data.</p>
 *
 * <p>NOTE: Balance is set on account creation via an initial balance and is subsequently only modified by transactions.
 * Therefore, the Account model allow for balances being set again by the user.</p>
 */
public class Account {
    private int account_id;
    private String account_name;
    private String first_name;
    private String last_name;
    private double balance;
    private int color;

    /**
     * Number of colors an account can be given. Colors are stored as an index rather than as a literal value
     * so that the palette itself lives in styles.css (.account-color-1 through .account-color-8, drawn from
     * the active theme) and the database stays independent of whichever theme is applied.
     */
    public static final int COLOR_COUNT = 8;

    /** Color used when none was chosen, including for accounts created before colors existed. */
    public static final int DEFAULT_COLOR = 1;

    /**
     * Given all account data, creates an Account object and assigns each parameter to its fields.
     * @param account_id   Account ID (22)
     * @param account_name Account Name ("Checking")
     * @param first_name   User First Name ("John")
     * @param last_name    User Last Name ("Doe")
     * @param balance      Account Balance (1000.00)
     * @param color        Palette index from 1 to COLOR_COUNT
     */
    public Account(int account_id, String account_name, String first_name, String last_name, double balance,
                   int color) {
        this.account_id = account_id;
        setAccountName(account_name);
        setFirstName(first_name);
        setLastName(last_name);
        this.balance = balance;
        setColor(color);
    }

    /**
     * Creates an Account with the default color, for callers that have no color to supply.
     * @param account_id   Account ID (22)
     * @param account_name Account Name ("Checking")
     * @param first_name   User First Name ("John")
     * @param last_name    User Last Name ("Doe")
     * @param balance      Account Balance (1000.00)
     */
    public Account(int account_id, String account_name, String first_name, String last_name, double balance) {
        this(account_id, account_name, first_name, last_name, balance, DEFAULT_COLOR);
    }

    /**
     * Returns the account's unique integer ID number.
     * @return account_id
     */
    public int getAccountID() { return account_id; }

    /**
     * Sets the account's name (e.g. Checking, Savings).
     * @param accountName New account name
     */
    public void setAccountName(String accountName) { this.account_name = accountName; }

    /**
     * Returns the account's name.
     * @return account_name
     */
    public String getAccountName() { return account_name; }

    /**
     * Sets the account user's first name.
     * @param firstName User's first name
     */
    public void setFirstName(String firstName) { this.first_name = firstName; }

    /**
     * Returns the account user's first name.
     * @return first_name
     */
    public String getFirstName() { return first_name; }

    /**
     * Sets the account user's last name.
     * @param lastName User's last name
     */
    public void setLastName(String lastName) { this.last_name = lastName; }

    /**
     * Returns the account user's last name.
     * @return last_name
     */
    public String getLastName() { return last_name; }

    /**
     * Returns the user's account balance as a double.
     * NOTE: Balance is only modified through transactions via DatabaseManager.
     * @return balance
     */
    public double getBalance() { return balance; }

    /**
     * Returns the account's palette index, used to pick its .account-color-N style class.
     * @return color
     */
    public int getColor() { return color; }

    /**
     * Sets the account's palette index. Values outside the palette fall back to the default rather than being
     * stored as-is, so a stale or hand-edited database value cannot leave a tile with no color styling.
     * @param color Palette index from 1 to COLOR_COUNT
     */
    public void setColor(int color) {
        this.color = (color < 1 || color > COLOR_COUNT) ? DEFAULT_COLOR : color;
    }

    /**
     * Displays the Account in a readable format.
     * Example: Student Checking (ID#22) - John Doe - $1000.00
     * @return Account data formatted as a String
     */
    @Override
    public String toString() {
        return account_name + " (ID#" + account_id + ") - " + first_name + " " + last_name
                + " - $" + String.format("%.2f", balance);
    }
}