package org.jsmart.steply.cli;

import org.apache.commons.cli.*;
import org.jsmart.steply.core.SteplyScenarioRunner;

import java.io.File;
import java.util.Map;

/**
 * Minimal CLI that parses args and invokes SteplyScenarioRunner.
 */
public class SteplyCLI {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("s").longOpt("scenario").hasArg().desc("Single scenario file path").build());
        options.addOption(Option.builder("f").longOpt("folder").hasArg().desc("Folder containing multiple scenarios").build());
        options.addOption(Option.builder("t").longOpt("target").hasArg().desc("Target environment properties file").required().build());
        options.addOption(Option.builder("r").longOpt("reports").hasArg().desc("Custom report output directory (default: ./target)").build());
        options.addOption(Option.builder("l").longOpt("log-level").hasArg().desc("Logging level (WARN/INFO/DEBUG)").build());
        options.addOption("v", "version", false, "Show version information");
        options.addOption("h", "help", false, "Show help");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                new HelpFormatter().printHelp("steply", options);
                System.exit(0);
            }

            if (cmd.hasOption("v")) {
                System.out.println("Steply Test Execution v0.1.0-SNAPSHOT");
                System.exit(0);
            }

            String scenario = cmd.getOptionValue("s");
            String folder = cmd.getOptionValue("f");
            String target = cmd.getOptionValue("t");
            String reports = cmd.getOptionValue("r", "target");
            String logLevel = cmd.getOptionValue("l", "INFO");

            if ((scenario == null && folder == null) || (scenario != null && folder != null)) {
                System.err.println("Either --scenario (-s) OR --folder (-f) must be provided (mutually exclusive).");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            if (scenario != null) {
                // Single scenario mode
                SteplyScenarioRunner runner = new SteplyScenarioRunner(scenario, target, reports, logLevel);
                Map<String, Object> results = runner.runSingleScenario();

                printSummary(results, scenario, target, reports);
                int failed = ((Number) results.getOrDefault("failed", 0)).intValue();
                System.exit(failed == 0 ? 0 : 2);
            } else {
                // Folder mode (MVP: iterate files and call runner for each)
                File folderFile = new File(folder);
                if (!folderFile.isDirectory()) {
                    System.err.println("Provided folder path is not a directory: " + folder);
                    System.exit(1);
                }
                File[] files = folderFile.listFiles((d, name) -> name.endsWith(".json"));
                int totalFailed = 0;
                if (files != null) {
                    for (File f : files) {
                        SteplyScenarioRunner runner = new SteplyScenarioRunner(f.getAbsolutePath(), target, reports, logLevel);
                        Map<String, Object> results = runner.runSingleScenario();
                        totalFailed += ((Number) results.getOrDefault("failed", 0)).intValue();
                    }
                }
                System.exit(totalFailed == 0 ? 0 : 2);
            }
        } catch (ParseException pe) {
            System.err.println("Error parsing arguments: " + pe.getMessage());
            new HelpFormatter().printHelp("steply", options);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }

    private static void printSummary(Map<String, Object> results, String scenario, String target, String reports) {
        System.out.println("========================================");
        System.out.println("Steply Test Execution v0.1.0-SNAPSHOT");
        System.out.println("========================================");
        System.out.println("Scenario: " + scenario);
        System.out.println("Target: " + target);
        System.out.println("Report: " + reports);
        System.out.println("========================================");
        System.out.println("Executing tests...");
        System.out.println();
        System.out.println(String.format("Total: %s", results.getOrDefault("total", 0)));
        System.out.println(String.format("Passed: %s", results.getOrDefault("passed", 0)));
        System.out.println(String.format("Failed: %s", results.getOrDefault("failed", 0)));
        System.out.println(String.format("Duration: %sms", results.getOrDefault("duration_ms", 0)));
        System.out.println("========================================");
        System.out.println("Reports generated at: " + reports + File.separator + "steply-report");
        System.out.println("========================================");
    }
}