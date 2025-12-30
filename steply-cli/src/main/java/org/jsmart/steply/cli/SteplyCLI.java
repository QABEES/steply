package org.jsmart.steply.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsmart.steply.core.SteplyScenarioRunner;

import java.io.File;

/**
 * Minimal CLI that parses args and invokes SteplyScenarioRunner.
 */
public class SteplyCLI {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("s").longOpt("scenario").hasArg().desc("Single scenario file path").build());
        options.addOption(Option.builder("f").longOpt("folder").hasArg().desc("Folder containing multiple scenarios").build());
        options.addOption(Option.builder("t").longOpt("target").hasArg().desc("Target environment properties file").build());
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

//            String scenario = "/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply/steply-core/src/main/resources/helloworld/hello_world_status_ok_assertions_new.json";
//            String target = "/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply/steply-core/src/main/resources/config/github_host_new.properties";
//            String folder = null;

            String scenario = cmd.getOptionValue("s");
            String folder = cmd.getOptionValue("f");
            String target = cmd.getOptionValue("t");

            String reports = cmd. getOptionValue("r", "target");
            String logLevel = cmd.getOptionValue("l", "INFO");

            if ((scenario == null && folder == null) || (scenario != null && folder != null)) {
                System.err.println("Either --scenario (-s) OR --folder (-f) must be provided (mutually exclusive).");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            if (target == null) {
                System. err.println("Missing required option:  --target (-t)");
                new HelpFormatter().printHelp("steply", options);
                System.exit(1);
            }

            if (scenario != null) {

                // Single scenario mode
                SteplyScenarioRunner runner = new SteplyScenarioRunner(scenario, target, reports, logLevel);
                runner.runSingleScenario();
            } else {
                // Folder mode:  iterate . json files and run each
                File folderFile = new File(folder);
                if (!folderFile.isDirectory()) {
                    System. err.println("Provided folder path is not a directory: " + folder);
                    System.exit(1);
                }

                // TODO: Implement using Package runner

            }
        } catch (ParseException pe) {
            System.err. println("Error parsing arguments: " + pe.getMessage());
            new HelpFormatter().printHelp("steply", options);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System. exit(2);
        }
    }

}