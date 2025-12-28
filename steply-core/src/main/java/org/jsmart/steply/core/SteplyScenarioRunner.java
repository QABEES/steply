package org.jsmart.steply.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> runSingleScenario() throws IOException {
        validate();
        SteplyConfigLoader configLoader = new SteplyConfigLoader();
        configLoader.updateLoggingLevel(logLevel);
        Map<String, String> envProps = configLoader.loadEnvironmentProperties(targetEnvFile.getAbsolutePath());

        // Parse scenario JSON (basic)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode scenario = mapper.readTree(scenarioFile);

        // MVP: we don't execute HTTP steps here. Instead we validate and create a stubbed result.
        Map<String, Object> results = new HashMap<>();
        results.put("scenario", scenarioFile.getName());
        results.put("target", targetEnvFile.getName());
        results.put("host", envProps.getOrDefault("host", ""));
        results.put("total", 1);
        results.put("passed", 1);
        results.put("failed", 0);
        results.put("duration_ms", 0);

        // Generate reports
        SteplyReportGenerator rg = new SteplyReportGenerator();
        File out = new File(reportDir, "steply-report");
        rg.generateHTMLReport(results, out);
        rg.generateCSVReport(results, out);

        return results;
    }
}