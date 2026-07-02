package org.jsmart.steply;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUCoreTestRunner {

    public static boolean runSingle(String scenarioPath, String targetEnvPath) {
        // ---- Override ZeroCode annotations programmatically via System properties ----
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.scenario", scenarioPath);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(ScenarioTest.class);

        // Print failures (if any)
        printFailedSummary(result);

        // Print final stats
        printFinalStats(result);

        // ---- IMPORTANT: return success flag for CLI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            return false;
        }

        System.out.println("✅ Tests passed");
        return true;
    }

    public static boolean runSuite(String folder, String targetEnvPath) {
        // Override ZeroCode annotations programmatically via System properties
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.folder", folder);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(SuiteTest.class);

        // Print failures (if any)
        printFailedSummary(result);

        // Print final stats
        printFinalStats(result);

        // ---- IMPORTANT: return success flag for CLI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            return false;
        }

        System.out.println("✅ Tests passed");
        return true;
    }

    private static void printFinalStats(Result result) {
        System.out.println();
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("[TEST] SUMMARY");
        System.out.println("----------------------------------------------------------------------------------------");

        System.out.printf("[TEST] Run Count ........................................ [%d]%n", result.getRunCount());
        System.out.printf("[TEST] Failure Count .................................... [%d]%n", result.getFailureCount());

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("[TEST] Reports: target/*.html");
        System.out.println("[TEST] Audit Logs: target/logs/*.log");
        System.out.println("----------------------------------------------------------------------------------------");
    }
    private static void printFailedSummary(Result result) {
        if (result.getFailureCount() > 0) {
            System.out.println("----------------------------------------------------------------------------------------");
            System.out.println("FAILURES:" + "\n--------");
            int count = 0;
            for (Failure failure : result.getFailures()) {
                System.out.println( (++count) + ") Test failed(❌): " + failure.getTestHeader());
            }
        }
    }

/*
    public static void main(String[] args) {
        String scenarioPath = "helloworld/hello_world_status_ok_assertions_new.json";
        String targetEnvPath = "config/github_host_new.properties";
        runSingle(scenarioPath, targetEnvPath);

        String folder = "helloworldnew" ;
        String targetEnvPath = "config/github_host_new.properties";
        runSuite(folder, targetEnvPath);

    }
*/
}

