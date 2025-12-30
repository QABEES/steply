package org.jsmart.steply.core;

import org.jsmart.steply.template.RunJUnitTestProgrammatically;

import java.io.File;

/**
 * MVP SteplyScenarioRunner. Validates inputs, parses scenario and target env,
 * and produces a simple pass/fail summary. This is intentionally lightweight:
 * TODO-- integration code to kick off zerocode-tdd programmatically can be added later.
 */
public class SteplyScenarioRunner {

    private final File scenarioFile;
    private final File targetEnvFile;
    private final File reportDir;
    private final String logLevel;

    public SteplyScenarioRunner(String scenarioPath, String targetEnvPath, String reportDirPath, String logLevel) {
        this.scenarioFile = new File(scenarioPath);
        this.targetEnvFile = new File(targetEnvPath);
        this.reportDir = new File(reportDirPath != null ? reportDirPath : "target");
        this.logLevel = logLevel != null ? logLevel : "INFO";
    }

    public void validate() {
        if (!scenarioFile.exists()) {
            throw new IllegalArgumentException("Scenario file does not exist: " + scenarioFile.getAbsolutePath());
        }
        if (!targetEnvFile.exists()) {
            throw new IllegalArgumentException("Target env file does not exist: " + targetEnvFile.getAbsolutePath());
        }
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
    }

    public void runSingleScenario() {
        validate();
        RunJUnitTestProgrammatically.run(scenarioFile.getAbsolutePath(), targetEnvFile.getAbsolutePath());
    }
}