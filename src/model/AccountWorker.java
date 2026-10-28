
package model;

import exception.BankException;

public class AccountWorker implements Runnable {

    private Account account;
    private int times;
    private long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        System.out.println(
                Thread.currentThread().getName() + " started.");

        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (BankException e) {
                System.out.println("Deposit failed: " + e.getMessage());
                break;
            }
        }

        System.out.println(
                Thread.currentThread().getName() + " finished.");
    }
}
