package org.jsmart.steply.core;

import org.jsmart.steply.template.TestRunner;

import java.io.File;

/**
 * MVP SteplyScenarioRunner. Validates inputs, parses scenario and target env,
 * and produces a simple pass/fail summary. This is intentionally lightweight:
 * TODO-- integration code to kick off zerocode-tdd programmatically can be added later.
 */
public class SteplyCommandRunner {

    private final File scenarioFile;
    private final File suiteFolder;
    private final File targetEnvFile;
    private final File reportDir;
    private final String logLevel;

    public SteplyCommandRunner(String scenarioPath, String suiteFolder, String targetEnvPath, String reportDirPath, String logLevel) {
        if (null != scenarioPath) {
            this.scenarioFile = new File(scenarioPath);
        } else {
            this.scenarioFile = null;
        }
        if(null != suiteFolder) {
            this.suiteFolder = new File(suiteFolder);
        } else {
            this.suiteFolder = null;
        }
        this.targetEnvFile = new File(targetEnvPath);
        this.reportDir = new File(reportDirPath != null ? reportDirPath : "target");
        this.logLevel = logLevel != null ? logLevel : "INFO";
    }

    public void validate() {
        if (null != scenarioFile && !scenarioFile.exists()) {
            throw new IllegalArgumentException("Test Scenario file does not exist: " + scenarioFile.getAbsolutePath());
        }

        if (null != suiteFolder && !suiteFolder.exists()) {
            throw new IllegalArgumentException("Test Suite folder does not exist: " + suiteFolder.getAbsolutePath());
        }

        if (null != targetEnvFile && !targetEnvFile.exists()) {
            throw new IllegalArgumentException("Target env file does not exist: " + targetEnvFile.getAbsolutePath());
        }

        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
    }

    public void runSingleScenario() {
        validate();
        if (scenarioFile == null) {
            throw new IllegalStateException("Scenario file must be provided for single scenario execution");
        }
        TestRunner.runSingle(scenarioFile.getAbsolutePath(), targetEnvFile.getAbsolutePath());
    }

    public void runSuite() {
        validate();
        if (suiteFolder == null) {
            throw new IllegalStateException("Suite folder must be provided for suite execution");
        }
        TestRunner.runSuite(suiteFolder.getAbsolutePath(), targetEnvFile.getAbsolutePath());
    }
}