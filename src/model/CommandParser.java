package model;
import util.Validator;
public class CommandParser {

    public static Command parse(String line) {

        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be empty");
        }

        String[] parts = line.trim().split("\\s+");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Enter: TYPE ACCOUNT_NUMBER AMOUNT");
        }

        TransactionType type;

        try {
            type = TransactionType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid transaction type");
        }

        if (!parts[1].matches("AC[0-9]{4}")) {
            throw new IllegalArgumentException(
                    "Invalid account number");
        }

        if (!Validator.isValidAmount(parts[2])) {
            throw new IllegalArgumentException(
                    "Amount must be a positive whole number");
        }

        long amount;

        try {
            amount = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount is too large");
        }

        return new Command(type, parts[1], amount);
    }
}
