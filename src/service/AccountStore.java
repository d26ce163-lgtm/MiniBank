
package service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import model.Account;

public class AccountStore {

    private HashMap<String, Account> accounts = new HashMap<>();

    private ArrayList<String> transactionHistory = new ArrayList<>();

    // Add an account
    public void add(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    // Find an account using its account number
    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    // Get all accounts
    public ArrayList<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    // Add a transaction to history
    public void addTransaction(String transaction) {
        transactionHistory.add(transaction);
    }

    // Get transaction history
    public ArrayList<String> getHistory() {
        return new ArrayList<>(transactionHistory);
    }

    // Sort accounts by balance, highest first
    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> sortedAccounts = getAll();

        sortedAccounts.sort(
                Comparator.comparingLong(Account::getBalance).reversed()
        );

        return sortedAccounts;
    }

    // Supplementary: Find accounts above a given balance
    public ArrayList<Account> getAccountsAbove(long amount) {
        ArrayList<Account> result = new ArrayList<>();

        for (Account account : accounts.values()) {
            if (account.getBalance() > amount) {
                result.add(account);
            }
        }

        return result;
    }
}
