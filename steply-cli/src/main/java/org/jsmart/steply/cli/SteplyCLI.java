package org.jsmart.steply.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsmart.steply.core.SteplyCommandRunner;

/**
 * Minimal CLI that parses args and invokes SteplyCommandRunner.
 */
public class SteplyCLI {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("s").longOpt("scenario").hasArg().desc("Single scenario file path").build());
        options.addOption(Option.builder("f").longOpt("folder").hasArg().desc("Folder(Test Suite) containing multiple scenarios").build());
        options.addOption(Option.builder("t").longOpt("target-env").hasArg().desc("Target environment properties file").build());
        options.addOption(Option.builder("hc").longOpt("host").hasArg().desc("Host(s) configuration properties file path").build());
        options.addOption(Option.builder("r").longOpt("reports").hasArg().desc("Custom report output directory (default is ./target)").build());
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
            String suiteFolder = cmd.getOptionValue("f");
            String targetEnv = cmd.getOptionValue("t");
            String hostConfig = cmd.getOptionValue("hc");

            String reports = cmd.getOptionValue("r", "target");
            String logLevel = cmd.getOptionValue("l", "INFO");

            if ((scenario == null && suiteFolder == null) || (scenario != null && suiteFolder != null)) {
                System.err.println("Either --scenario (-s) OR --folder (-f) must be provided (mutually exclusive).");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            if (targetEnv == null && hostConfig == null) {
                System.err.println("Missing required option: either --target-env (-t) OR --host (-hc) must be provided.");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            if (targetEnv != null && hostConfig != null) {
                System.err.println("Only one of --target-env (-t) OR --host (-hc) should be provided (mutually exclusive).");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            // Because both are same, just handling one option is good enough.
            if (hostConfig != null) {
                targetEnv = hostConfig;
            }

            if (suiteFolder != null) {
                // Suite mode
                SteplyCommandRunner runner = new SteplyCommandRunner(null, suiteFolder, targetEnv, reports, logLevel);
                runner.runSuite();
            }

            if (scenario != null) {
                // Single scenario mode
                SteplyCommandRunner runner = new SteplyCommandRunner(scenario, null, targetEnv, reports, logLevel);
                runner.runSingleScenario();
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

}