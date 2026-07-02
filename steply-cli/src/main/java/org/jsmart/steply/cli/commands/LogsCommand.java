package org.jsmart.steply.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "logs", description = "Show logs of the last runs", mixinStandardHelpOptions = true)
public class LogsCommand implements Callable<Integer> {

    @Option(names = "--tail", description = "streams/follows the log output")
    private boolean tail;

    @Override
    public Integer call() throws Exception {
        Path logDir = Paths.get("target", "logs");
        if (!Files.exists(logDir)) {
            logDir = Paths.get("target");
            if (!Files.exists(logDir)) {
                System.err.println("No logs directory found.");
                return 1;
            }
        }

        // Find the latest *test*.log file
        Optional<Path> latestLogFile;
        try (java.util.stream.Stream<Path> stream = Files.walk(logDir, 2)) {
            latestLogFile = stream
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".log") && name.contains("test");
                    })
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
        }

        if (latestLogFile.isEmpty()) {
            System.err.println("No test log files found in " + logDir.toAbsolutePath());
            return 1;
        }

        File logFile = latestLogFile.get().toFile();
        System.out.println("Reading log file: " + logFile.getAbsolutePath());

        if (tail) {
            tailFile(logFile);
        } else {
            printFile(logFile);
        }
        return 0;
    }

    private void printFile(File logFile) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private void tailFile(File logFile) throws Exception {
        // TODO: v1 limitation - this tail implementation does not handle log rotation.
        // If a new test run starts and rotates the log file, this will keep reading the old file handle.
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    System.out.println(line);
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
