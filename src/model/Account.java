
package model;

import java.io.Serializable;
import java.util.Objects;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.Positive;

public abstract class Account
        implements Transactable, InterestBearing, Serializable, Comparable<Account> {

    private static final long serialVersionUID = 1L;

    @Id
    private final String accountNumber;

    private String ownerName;

    @Positive
    protected long balance;

    protected boolean active;

    private static long accountCounter = 0;

    private static String generateAccountNumber() {
        accountCounter++;
        return String.format("AC%04d", accountCounter);
    }

    public Account(String ownerName, long openingBalance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
    }

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    @Override
    public synchronized void deposit(long amount) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be greater than zero.");
        }

        if (!active) {
            throw new BankException("Account is inactive.");
        }

        balance += amount;
    }

    @Override
    public synchronized boolean withdraw(long amount) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException(
                    "Withdrawal amount must be greater than zero.");
        }

        if (!active) {
            throw new BankException("Account is inactive.");
        }

        if (balance < amount) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException(shortfall);
        }

        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException(amount);
        }

        balance -= amount;
        return true;
    }

    public void transfer(Account to, long amount) throws BankException {
        if (to == null) {
            throw new BankException("Destination account not found.");
        }

        if (to == this) {
            throw new BankException("Cannot transfer to the same account.");
        }

        try {
            withdraw(amount);
            to.deposit(amount);
            System.out.println("Transfer successful.");
        } catch (BankException e) {
            throw e;
        } finally {
            System.out.println("Transfer operation completed.");
        }
    }

    public abstract boolean canWithdraw(long amount);

    @Override
    public abstract double interestRate();

    @Override
    public long getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isActive() {
        return active;
    }

    public void closeAccount() {
        active = false;
    }

    // Practical 12: Natural ordering by account number
    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public String toString() {
        return accountNumber + " | " + ownerName + " | " + balance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Account)) {
            return false;
        }

        Account other = (Account) obj;
        return accountNumber.equals(other.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}
