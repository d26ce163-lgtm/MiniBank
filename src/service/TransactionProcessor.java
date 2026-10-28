
package service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import model.Account;

public class TransactionProcessor {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    // Practical 12: Thread-safe shared account storage
    private final ConcurrentHashMap<String, Account> accounts =
            new ConcurrentHashMap<>();

    // Existing thread-safe task counter
    private final Map<String, AtomicInteger> taskCounts =
            new ConcurrentHashMap<>();

    // Add an account to shared storage
    public void addAccount(Account account) {
        if (account != null) {
            accounts.put(account.getAccountNumber(), account);
        }
    }

    // Find an account by account number
    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    // Get all shared accounts
    public Map<String, Account> getAccounts() {
        return Map.copyOf(accounts);
    }

    // Submit a task to the thread pool
    public void submit(Runnable task) {

        executor.execute(() -> {

            String threadName =
                    Thread.currentThread().getName();

            taskCounts.computeIfAbsent(
                    threadName,
                    name -> new AtomicInteger()
            ).incrementAndGet();

            task.run();
        });
    }

    // Stop the thread pool and display task counts
    public void stop() {

        executor.shutdown();

        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println(
                        "WARNING: Tasks are taking too long. "
                        + "Stopping remaining tasks.");

                executor.shutdownNow();

                if (!executor.awaitTermination(
                        5, TimeUnit.SECONDS)) {
                    System.out.println(
                            "WARNING: Some tasks did not stop.");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("\n--- Thread Pool Task Counts ---");

        for (Map.Entry<String, AtomicInteger> entry
                : taskCounts.entrySet()) {

            System.out.println(
                    entry.getKey() + " executed "
                    + entry.getValue().get() + " tasks");
        }
    }
}
