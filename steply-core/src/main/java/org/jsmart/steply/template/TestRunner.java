package org.jsmart.steply.template;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {

    public static void runSingle(String scenarioPath, String targetEnvPath) {
        // ---- Override ZeroCode annotations programmatically via System properties ----
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.scenario", scenarioPath);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(SingleTest.class);

        // Print failures (if any)
        printFailedSummary(result);

        // Print final stats
        printResultStats(result);

        // ---- IMPORTANT: exit code for CI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            System.exit(1);   // NON-ZERO → CI FAIL
        }

        System.out.println("✅ Tests passed");
        System.exit(0);       // ZERO → CI PASS
    }

    public static void runSuite(String folder, String targetEnvPath) {
        // Override ZeroCode annotations programmatically via System properties
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.folder", folder);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(SuiteTest.class);

        // Print failures (if any)
        printFailedSummary(result);

        // Print final stats
        printResultStats(result);

        // ---- IMPORTANT: exit code for CI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            System.exit(1);   // NON-ZERO → CI FAIL
        }

        System.out.println("✅ Tests passed");
        System.exit(0);       // ZERO → CI PASS
    }

    private static void printResultStats(Result result) {
        System.out.println("SUMMARY:" + "\n--------");
        System.out.println("Run count: " + result.getRunCount());
        System.out.println("Failure count: " + result.getFailureCount());
        System.out.println("------------------------------------------------------------------------------------------------");
    }

    private static void printFailedSummary(Result result) {
        System.out.println("------------------------------------------------------------------------------------------------");
        if (result.getFailureCount() > 0) {
            System.out.println("FAILURES:" + "\n---------");
            int count = 0;
            for (Failure failure : result.getFailures()) {
                System.out.println( (++count) + ") Test failed(❌): " + failure.getTestHeader());
            }
            System.out.println("\n----");
        }
    }

    public static void main(String[] args) {
//        String scenarioPath = "helloworld/hello_world_status_ok_assertions_new.json";
//        String targetEnvPath = "config/github_host_new.properties";
//        runSingle(scenarioPath, targetEnvPath);

        String folder = "helloworldnew" ;
        String targetEnvPath = "config/github_host_new.properties";
        runSuite(folder, targetEnvPath);

    }
}

