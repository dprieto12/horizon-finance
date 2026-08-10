package models;

/**
 * Class representing Accounts, holding only account info and no transaction data. Used by Main and DatabaseManager
 * in order to communicate data between the client and the database. DatabaseManager converts SQL data stored in the
 * accounts table to Account objects and uses altered Account objects to update the SQL database.
 *
 * NOTE: Balance is set on account creation via an initial balance and is subsequently only modified by transactions.
 * Direct balance updates are intentionally not exposed through the UI to preserve data integrity.
 */
public class Account {
    private int account_id;
    private String account_name;
    private String first_name;
    private String last_name;
    private double balance;

    /**
     * Given all account data, creates an Account object and assigns each parameter to its fields.
     * @param account_id   Account ID (22)
     * @param account_name Account Name ("Checking")
     * @param first_name   User First Name ("John")
     * @param last_name    User Last Name ("Doe")
     * @param balance      Account Balance (1000.00)
     */
    public Account(int account_id, String account_name, String first_name, String last_name, double balance) {
        this.account_id = account_id;
        setAccountName(account_name);
        setFirstName(first_name);
        setLastName(last_name);
        this.balance = balance;
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