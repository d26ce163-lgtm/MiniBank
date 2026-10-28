
package service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import model.Account;
import model.SavingsAccount;
import exception.BankException;

public class DeadlockDemo {

    public static void runDemo() {

        System.out.println("\n--- Deadlock Demonstration ---");

        Account accountA =
                new SavingsAccount("Deadlock-A", 1000, 0);

        Account accountB =
                new SavingsAccount("Deadlock-B", 1000, 0);

        CountDownLatch bothLocked = new CountDownLatch(2);

        Thread t1 = new Thread(
                () -> transferWithOppositeOrder(
                        accountA, accountB, 100, bothLocked),
                "Transfer-A-to-B");

        Thread t2 = new Thread(
                () -> transferWithOppositeOrder(
                        accountB, accountA, 100, bothLocked),
                "Transfer-B-to-A");

        // Allow the application to exit despite the intentional deadlock.
        t1.setDaemon(true);
        t2.setDaemon(true);

        t1.start();
        t2.start();

        try {
            boolean bothStarted =
                    bothLocked.await(3, TimeUnit.SECONDS);

            if (bothStarted) {
                Thread.sleep(500);
            }

            if (t1.getState() == Thread.State.BLOCKED
                    && t2.getState() == Thread.State.BLOCKED) {

                System.out.println("Deadlock detected!");

                System.out.println(
                        "Both threads are waiting for each other's account lock.");

            } else {
                System.out.println(
                        "Deadlock was not confirmed. Try running again.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(
                    "Deadlock demonstration interrupted.");
        }

        // Deadlock prevention test
        System.out.println("\n--- Deadlock Prevention ---");

        Account accountC =
                new SavingsAccount("Fixed-A", 1000, 0);

        Account accountD =
                new SavingsAccount("Fixed-B", 1000, 0);

        Thread t3 = new Thread(
                () -> transferWithFixedOrder(
                        accountC, accountD, 100),
                "Fixed-Transfer-A-to-B");

        Thread t4 = new Thread(
                () -> transferWithFixedOrder(
                        accountD, accountC, 100),
                "Fixed-Transfer-B-to-A");

        t3.start();
        t4.start();

        try {
            long startTime = System.currentTimeMillis();

            t3.join(3000);
            t4.join(3000);

            long elapsedTime =
                    System.currentTimeMillis() - startTime;

            if (t3.isAlive() || t4.isAlive()) {

                System.out.println(
                        "WARNING: Long-running transfer detected!");

                System.out.println(
                        "A transfer is taking longer than expected.");

            } else {

                System.out.println(
                        "Both transfers completed successfully.");

                System.out.println(
                        "Account C balance: Rs. "
                        + accountC.getBalance());

                System.out.println(
                        "Account D balance: Rs. "
                        + accountD.getBalance());
            }

            System.out.println(
                    "Transfer test time: "
                    + elapsedTime + " ms");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(
                    "Transfer prevention test interrupted.");
        }
    }

    // Intentionally acquires account locks in opposite orders.
    private static void transferWithOppositeOrder(
            Account from,
            Account to,
            long amount,
            CountDownLatch bothLocked) {

        synchronized (from) {

            System.out.println(
                    Thread.currentThread().getName()
                    + " locked "
                    + from.getAccountNumber());

            bothLocked.countDown();

            try {
                bothLocked.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            synchronized (to) {
                completeTransfer(from, to, amount);
            }
        }
    }

    // Always acquires account locks in account-number order.
    private static void transferWithFixedOrder(
            Account from,
            Account to,
            long amount) {

        Account first;
        Account second;

        if (from.getAccountNumber().compareTo(
                to.getAccountNumber()) < 0) {

            first = from;
            second = to;

        } else {
            first = to;
            second = from;
        }

        synchronized (first) {
            synchronized (second) {
                completeTransfer(from, to, amount);
            }
        }
    }

    // Performs the transfer while both required locks are held.
    private static void completeTransfer(
            Account from,
            Account to,
            long amount) {

        try {
            boolean success = from.withdraw(amount);

            if (success) {
                to.deposit(amount);

                System.out.println(
                        Thread.currentThread().getName()
                        + " transferred Rs. " + amount);
            } else {
                System.out.println(
                        "Withdrawal was not completed.");
            }

        } catch (BankException e) {
            System.out.println(
                    "Transfer failed: " + e.getMessage());
        }
    }
}
