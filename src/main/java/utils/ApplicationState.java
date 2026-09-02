package utils;

import models.Account;

import java.util.Stack;

/**
 * <p>The primary purpose of this class is to store the current state of the program for the end-user so that fields
 * such as the current account can be accessed by multiple controllers.</p>
 *
 *  <p>This class was originally intended to also store the history of scenes (hence, ApplicationState) and if required
 *  in later updates, that functionality along with all other necessary state management code is to be implemented
 *  here.</p>
 */

public class ApplicationState {
    private static Account currentAccount;

    /**
     * Sets the currentAccount field to the passed Account object.
     * @param account Account object to set as the current account
     */

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    /**
     * Returns the instance of the currently selected account.
     * @return Account object of the currently selected account
     */

    public static Account getCurrentAccount() {
        return currentAccount;
    }
}
