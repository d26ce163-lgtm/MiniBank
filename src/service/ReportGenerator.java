
package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    public static void generate(Path logsFolder, Path reportFile)
            throws IOException {

        long totalDeposits = 0;
        long totalWithdrawals = 0;

        List<String> fileDetails = new ArrayList<>();

        if (Files.isDirectory(logsFolder)) {

            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(logsFolder)) {

                for (Path file : stream) {

                    String fileName =
                            file.getFileName().toString();

                    // Skip non-log files and directories.
                    if (!Files.isRegularFile(file)
                            || !fileName.endsWith(".log")) {
                        continue;
                    }

                    BasicFileAttributes attributes =
                            Files.readAttributes(
                                    file,
                                    BasicFileAttributes.class);

                    // Skip empty log files.
                    if (attributes.size() == 0) {
                        continue;
                    }

                    fileDetails.add("File: " + fileName);
                    fileDetails.add(
                            "Size: " + attributes.size() + " bytes");
                    fileDetails.add(
                            "Last modified: "
                                    + attributes.lastModifiedTime());
                    fileDetails.add("");

                    try (BufferedReader reader =
                                 Files.newBufferedReader(
                                         file,
                                         StandardCharsets.UTF_8)) {

                        String line;

                        while ((line = reader.readLine()) != null) {

                            String[] parts =
                                    line.trim().split("\\s+");

                            if (parts.length != 3) {
                                continue;
                            }

                            try {
                                long amount =
                                        Long.parseLong(parts[2]);

                                if (amount <= 0) {
                                    continue;
                                }

                                if (parts[0].equalsIgnoreCase(
                                        "DEPOSIT")) {

                                    totalDeposits += amount;

                                } else if (parts[0].equalsIgnoreCase(
                                        "WITHDRAW")) {

                                    totalWithdrawals += amount;
                                }

                            } catch (NumberFormatException e) {
                                // Ignore invalid amounts.
                            }
                        }
                    }
                }
            }
        }

        long netChange = totalDeposits - totalWithdrawals;

        StringBuilder report = new StringBuilder();

        report.append("========== MINIBANK END-OF-DAY REPORT ==========\n\n");
        report.append("Total Deposits: Rs. ")
                .append(totalDeposits).append("\n");
        report.append("Total Withdrawals: Rs. ")
                .append(totalWithdrawals).append("\n");
        report.append("Net Change: Rs. ")
                .append(netChange).append("\n\n");
        report.append("---------- LOG FILE DETAILS ----------\n");

        for (String detail : fileDetails) {
            report.append(detail).append("\n");
        }

        Path parent = reportFile.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                reportFile,
                report.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(report);
        System.out.println("Report saved to: " + reportFile);
    }
}
