package org.jsmart.steply.cli.commands;

import org.jsmart.steply.core.SteplyCommandRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Run a test scenario or suite", mixinStandardHelpOptions = true)
public class RunCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Scenario file", arity = "0..1")
    private File scenarioFileParam;

    @Option(names = {"-s", "--scenario"}, description = "Scenario file path (legacy)")
    private File scenarioFileOpt;

    @Option(names = {"-f", "--folder", "--suite"}, description = "Folder(Test Suite) containing multiple scenarios")
    private File suiteFolder;

    @Option(names = {"-t", "--target-env"}, description = "Target environment properties file")
    private File targetEnv;

    @Option(names = {"-hc", "--host"}, description = "Host(s) configuration properties file path")
    private File hostConfig;

    @Option(names = {"-r", "--reports"}, defaultValue = "target", description = "Custom report output directory (default is ./target)")
    private String reports;

    @Option(names = {"-l", "--log-level"}, defaultValue = "INFO", description = "Logging level (WARN/INFO/DEBUG)")
    private String logLevel;

    @Override
    public Integer call() throws Exception {
        File scenarioFile = scenarioFileParam != null ? scenarioFileParam : scenarioFileOpt;

        if (scenarioFile == null && suiteFolder == null) {
            System.err.println("Either a scenario file OR --folder/--suite must be provided.");
            spec.commandLine().usage(System.err);
            return 2;
        }

        if (scenarioFile != null && !scenarioFile.exists()) {
            System.err.println("Error: Scenario file does not exist: " + scenarioFile.getAbsolutePath());
            return 2;
        }

        if (suiteFolder != null && !suiteFolder.exists()) {
            System.err.println("Error: Suite folder does not exist: " + suiteFolder.getAbsolutePath());
            return 2;
        }

        File activeTargetEnv = targetEnv != null ? targetEnv : hostConfig;
        String targetEnvPath = null;
        if (activeTargetEnv != null) {
            targetEnvPath = activeTargetEnv.getAbsolutePath();
        } else {
            System.err.println("Steply: No --target-env (-t) OR --host (-hc) was provided. Running in default mode.");
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config/default.properties")) {
                if (in != null) {
                    File tmp = File.createTempFile("steply-default", ".properties");
                    tmp.deleteOnExit();
                    Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    targetEnvPath = tmp.getAbsolutePath();
                }
            }
        }

        try {
            SteplyCommandRunner runner = new SteplyCommandRunner(
                    scenarioFile != null ? scenarioFile.getAbsolutePath() : null,
                    suiteFolder != null ? suiteFolder.getAbsolutePath() : null,
                    targetEnvPath,
                    reports,
                    logLevel
            );
            
            boolean success;
            if (suiteFolder != null) {
                success = runner.runSuite();
            } else {
                success = runner.runSingleScenario();
            }
            return success ? 0 : 1;
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            return 2;
        }
    }
}

