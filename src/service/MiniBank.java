
package service;

import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import model.*;
import exception.BankException;
import exception.InsufficientFundsException;

public class MiniBank {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BankInfo info = new BankInfo("MiniBank", "Main Branch");

        System.out.println("================================");
        System.out.println("Welcome to " + info.name());
        System.out.println("Branch: " + info.branch());
        System.out.println("================================");

        // Practical 11: Save and load accounts
        Path dataFolder = Path.of("data");
        Path accountFile = dataFolder.resolve("accounts.dat");
        Path logsFolder = dataFolder.resolve("logs");
        Path reportFile = dataFolder.resolve("reports")
                .resolve("end_of_day_report.txt");

        Account[] accounts = {
            new SavingsAccount("Riya", 5000, 1000),
            new CurrentAccount("Aman", 1000, 2000),
            new FixedDepositAccount("Neha", 10000)
        };

        try {
            if (Files.exists(accountFile)) {
                accounts = StatePersister.load(accountFile);
                System.out.println("Existing accounts loaded successfully.");
            } else {
                StatePersister.save(accounts, accountFile);
                accounts = StatePersister.load(accountFile);
                System.out.println("Accounts saved and reloaded successfully.");
            }

            displayAccounts(accounts);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println(
                    "Account loading/saving error: " + e.getMessage());
        }

        // Practical 12: AccountStore
        System.out.println("\n--- Practical 12: AccountStore ---");

        AccountStore store = new AccountStore();

        for (Account account : accounts) {
            store.add(account);
        }

        String searchAccountNumber = accounts[1].getAccountNumber();
        Account foundAccount = store.get(searchAccountNumber);

        System.out.println("\nAccount lookup: " + searchAccountNumber);

        if (foundAccount != null) {
            System.out.println("Account found: " + foundAccount);
        } else {
            System.out.println("Account not found.");
        }

        System.out.println("\n--- Accounts Sorted by Balance ---");

        ArrayList<Account> sortedAccounts = store.sortedByBalance();

        for (Account account : sortedAccounts) {
            System.out.println(account);
        }

        System.out.println("\n--- Accounts With Balance Above Rs. 2000 ---");

        ArrayList<Account> highBalanceAccounts =
                store.getAccountsAbove(2000);

        for (Account account : highBalanceAccounts) {
            System.out.println(account);
        }

        System.out.println("\n--- Natural Ordering: Comparable ---");

        ArrayList<Account> accountsByNumber = store.getAll();
        accountsByNumber.sort(null);

        for (Account account : accountsByNumber) {
            System.out.println(account);
        }

        // Practical 13: Generic Repository
        System.out.println("\n--- Practical 13: Generic Repository ---");

        Repository<Account> accountRepository = new Repository<>();

        // Save all existing accounts
        accountRepository.saveAll(accounts);

        // Find an account by its ID
        String repositorySearchId = accounts[1].getAccountNumber();

        Account repositoryAccount =
                accountRepository.findById(repositorySearchId);

        System.out.println("\nAccount lookup using Repository: "
                + repositorySearchId);

        if (repositoryAccount != null) {
            System.out.println("Account found: " + repositoryAccount);
        } else {
            System.out.println("Account not found.");
        }

        // Display all repository accounts
        System.out.println("\n--- All Repository Accounts ---");

        for (Account account : accountRepository.findAll()) {
            System.out.println(account);
        }

        System.out.println(
                "\nTotal accounts in Repository: "
                + accountRepository.count());

        // Test the delete method without deleting a real account
        System.out.println("\n--- Repository Delete Test ---");

        Repository<Account> testRepository = new Repository<>();
        testRepository.save(accounts[0]);

        boolean deleted = testRepository.delete(
                accounts[0].getAccountNumber());

        System.out.println("Test account deleted: " + deleted);
        System.out.println("Remaining test accounts: "
                + testRepository.count());

        // Test bounded generic method
        System.out.println("\n--- Bounded Generic Method ---");

        Integer[] numbers = {10, 20, 30, 40};

        System.out.println(
                "Average of numbers: "
                + Repository.average(numbers));

        Double[] decimalNumbers = {10.5, 20.5, 30.5};

        System.out.println(
                "Average of decimal numbers: "
                + Repository.average(decimalNumbers));

        // Practical 10: WithdrawRule using anonymous class
        System.out.println("\n--- WithdrawRule: Anonymous Class ---");

        WithdrawRule rule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount > 0 && amount <= 5000;
            }
        };

        System.out.println(
                "Riya can withdraw Rs. 3000: "
                + rule.allow(accounts[0], 3000));

        // WithdrawRule using lambda
        System.out.println("\n--- WithdrawRule: Lambda ---");

        WithdrawRule lambdaRule =
                (account, amount) -> amount > 0 && amount <= 2000;

        System.out.println(
                "Riya can withdraw Rs. 1500: "
                + lambdaRule.allow(accounts[0], 1500));

        System.out.println(
                "Riya can withdraw Rs. 3000: "
                + lambdaRule.allow(accounts[0], 3000));

        // Interest demonstration
        System.out.println("\n--- Interest Demonstration ---");

        for (Account account : accounts) {
            if (account instanceof InterestBearing) {
                InterestBearing interest = (InterestBearing) account;

                System.out.println(
                        account.getOwnerName()
                        + " interest rate: "
                        + interest.interestRate() + "%");
            }
        }

        // Exception handling demonstration
        System.out.println("\n--- Exception Handling ---");

        Account testAccount =
                new SavingsAccount("TestUser", 1000, 0);

        try {
            testAccount.withdraw(5000);
        } catch (InsufficientFundsException e) {
            System.out.println(
                    "Insufficient funds: " + e.getMessage());
        } catch (BankException e) {
            System.out.println("Bank error: " + e.getMessage());
        }

        System.out.println(
                "Test account balance: Rs. "
                + testAccount.getBalance());

        // Practical 9: Multithreading
        System.out.println("\n--- Multithreading Test ---");

        Account threadAccount =
                new SavingsAccount("ThreadTest", 0, 0);

        Thread[] threads = new Thread[10];

        for (int i = 0; i < threads.length; i++) {
            AccountWorker worker =
                    new AccountWorker(threadAccount, 1000, 1);

            threads[i] = new Thread(worker, "Worker-" + (i + 1));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Multithreading test interrupted.");
                sc.close();
                return;
            }
        }

        System.out.println("Expected Balance: Rs. 10000");
        System.out.println(
                "Final Balance: Rs. " + threadAccount.getBalance());

        // Practical 12: Thread pool and ConcurrentHashMap
        System.out.println("\n--- Thread Pool Test ---");

        Account poolAccount =
                new SavingsAccount("PoolTest", 1000, 0);

        TransactionProcessor processor = new TransactionProcessor();

        for (Account account : accounts) {
            processor.addAccount(account);
        }

        processor.addAccount(poolAccount);

        Account sharedAccount =
                processor.getAccount(accounts[0].getAccountNumber());

        System.out.println(
                "ConcurrentHashMap lookup: "
                + (sharedAccount != null
                        ? sharedAccount.getAccountNumber()
                        : "Account not found"));

        for (int i = 1; i <= 5; i++) {
            processor.submit(() -> {
                try {
                    poolAccount.deposit(100);

                    System.out.println(
                            Thread.currentThread().getName()
                            + " deposited Rs. 100");
                } catch (BankException e) {
                    System.out.println(
                            "Deposit failed: " + e.getMessage());
                }
            });
        }

        for (int i = 1; i <= 5; i++) {
            processor.submit(() -> {
                try {
                    poolAccount.withdraw(50);

                    System.out.println(
                            Thread.currentThread().getName()
                            + " withdrew Rs. 50");
                } catch (BankException e) {
                    System.out.println(
                            "Withdrawal failed: " + e.getMessage());
                }
            });
        }

        processor.stop();

        System.out.println("Expected Balance: Rs. 1250");
        System.out.println(
                "Final Balance: Rs. " + poolAccount.getBalance());

        // Producer-consumer demonstration
        System.out.println("\n--- Producer-Consumer Test ---");

        TransactionBuffer buffer = new TransactionBuffer();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.produce(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.consume();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Producer-consumer test interrupted.");
            sc.close();
            return;
        }

        System.out.println("Producer-Consumer test completed.");

        // Deadlock demonstration and prevention
        DeadlockDemo.runDemo();

        // Interactive banking menu
        int choice;

        do {
            System.out.println("\n========== MINIBANK MENU ==========");
            System.out.println("1. Display Accounts");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Interest");
            System.out.println("5. Show Transaction History");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            choice = readInt(sc);

            switch (choice) {

                case 1:
                    displayAccounts(accounts);
                    break;

                case 2: {
                    Account account = selectAccount(sc, accounts);

                    if (account != null) {
                        System.out.print("Enter deposit amount: ");
                        long amount = readLong(sc);

                        try {
                            account.deposit(amount);

                            System.out.println("Deposit successful.");
                            System.out.println(
                                    "Current balance: Rs. "
                                    + account.getBalance());

                            String line = "DEPOSIT "
                                    + account.getAccountNumber()
                                    + " " + amount;

                            logTransaction(
                                    logsFolder, "DEPOSIT", account, amount);

                            store.addTransaction(line);

                        } catch (BankException e) {
                            System.out.println(
                                    "Deposit failed: " + e.getMessage());
                        }
                    }
                    break;
                }

                case 3: {
                    Account account = selectAccount(sc, accounts);

                    if (account != null) {
                        System.out.print("Enter withdrawal amount: ");
                        long amount = readLong(sc);

                        if (!rule.allow(account, amount)) {
                            System.out.println(
                                    "Withdrawal rejected by rule.");
                            System.out.println(
                                    "Maximum allowed amount: Rs. 5000");
                            break;
                        }

                        try {
                            boolean success = account.withdraw(amount);

                            if (success) {
                                System.out.println("Withdrawal successful.");
                                System.out.println(
                                        "Current balance: Rs. "
                                        + account.getBalance());

                                String line = "WITHDRAW "
                                        + account.getAccountNumber()
                                        + " " + amount;

                                logTransaction(
                                        logsFolder, "WITHDRAW", account, amount);

                                store.addTransaction(line);
                            }

                        } catch (BankException e) {
                            System.out.println(
                                    "Withdrawal failed: " + e.getMessage());
                        }
                    }
                    break;
                }

                case 4: {
                    Account account = selectAccount(sc, accounts);

                    if (account != null) {
                        if (account instanceof InterestBearing) {
                            InterestBearing interest =
                                    (InterestBearing) account;

                            System.out.println(
                                    "Interest rate: "
                                    + interest.interestRate() + "%");
                        } else {
                            System.out.println(
                                    "Interest is not available.");
                        }
                    }
                    break;
                }

                case 5:
                    System.out.println("\n--- Transaction History ---");

                    ArrayList<String> history = store.getHistory();

                    if (history.isEmpty()) {
                        System.out.println("No transactions in this session.");
                    } else {
                        for (String transaction : history) {
                            System.out.println(transaction);
                        }
                    }
                    break;

                case 6:
                    System.out.println("Thank you for using MiniBank!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 6);

        // Practical 11: Save final balances and generate report
        try {
            StatePersister.save(accounts, accountFile);
            ReportGenerator.generate(logsFolder, reportFile);
        } catch (IOException e) {
            System.out.println(
                    "Error saving accounts or generating report: "
                    + e.getMessage());
        }

        sc.close();
    }

    // Write a transaction to the log file
    public static void logTransaction(
            Path logsFolder, String type, Account account, long amount) {

        Path logFile = logsFolder.resolve("transactions.log");

        String line = type + " "
                + account.getAccountNumber() + " "
                + amount;

        try {
            TransactionLog.append(logFile, line);
        } catch (IOException e) {
            System.out.println(
                    "Could not write transaction log: " + e.getMessage());
        }
    }

    // Display all accounts
    public static void displayAccounts(Account[] accounts) {

        System.out.println("\n--- Account Details ---");

        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];

            System.out.println(
                    (i + 1)
                    + ". Owner: " + account.getOwnerName()
                    + " | Account Number: " + account.getAccountNumber()
                    + " | Balance: Rs. " + account.getBalance());
        }
    }

    // Select an account
    public static Account selectAccount(
            Scanner sc, Account[] accounts) {

        displayAccounts(accounts);

        System.out.print(
                "Select account number (1-"
                + accounts.length + "): ");

        int number = readInt(sc);

        if (number < 1 || number > accounts.length) {
            System.out.println("Invalid account selection.");
            return null;
        }

        return accounts[number - 1];
    }

    // Read integer input
    public static int readInt(Scanner sc) {

        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid whole number: ");
            sc.next();
        }

        return sc.nextInt();
    }

    // Read amount input
    public static long readLong(Scanner sc) {

        while (!sc.hasNextLong()) {
            System.out.print("Enter a valid amount: ");
            sc.next();
        }

        return sc.nextLong();
    }
}
