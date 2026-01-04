package org.jsmart.steply.template;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class RunJUnitTestProgrammatically {

    public static void runSingle(String scenarioPath, String targetEnvPath) {
        // ---- Override ZeroCode annotations programmatically via System properties ----
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.scenario", scenarioPath);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(HelloWorldTest.class);

        // Print summary
        System.out.println("Run count: " + result.getRunCount());
        System.out.println("Failure count: " + result.getFailureCount());
        System.out.println("Ignored count: " + result.getIgnoreCount());
        System.out.println("Run time (ms): " + result.getRunTime());
        System.out.println("Successful: " + result.wasSuccessful());

        // Print failures (if any)
        for (Failure failure : result.getFailures()) {
            System.out.println("Test failed: " + failure.getTestHeader());
            System.out.println("Reason: " + failure.getMessage());
            System.out.println(failure.getTrace());
        }

        // ---- Print failures ----
        for (Failure failure : result.getFailures()) {
            System.err.println(failure.getTestHeader());
            System.err.println(failure.getMessage());
        }

        // ---- IMPORTANT: exit code for CI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            System.exit(1);   // NON-ZERO → CI FAIL
        }

        System.out.println("✅ Tests passed");
        System.exit(0);       // ZERO → CI PASS
    }

    public static void runSuite(String folder, String targetEnvPath) {
        // ---- Override ZeroCode annotations programmatically via System properties ----
        System.setProperty("zerocode.env", targetEnvPath);
        System.setProperty("zerocode.folder", folder);

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(HelloWorldSuiteTest.class);

        // Print summary
        System.out.println("Run count: " + result.getRunCount());
        System.out.println("Failure count: " + result.getFailureCount());
        System.out.println("Ignored count: " + result.getIgnoreCount());
        System.out.println("Run time (ms): " + result.getRunTime());
        System.out.println("Successful: " + result.wasSuccessful());

        // Print failures (if any)
        for (Failure failure : result.getFailures()) {
            System.out.println("Test failed: " + failure.getTestHeader());
            System.out.println("Reason: " + failure.getMessage());
            System.out.println(failure.getTrace());
        }

        // ---- Print failures ----
        for (Failure failure : result.getFailures()) {
            System.err.println(failure.getTestHeader());
            System.err.println(failure.getMessage());
        }

        // ---- IMPORTANT: exit code for CI ----
        if (!result.wasSuccessful()) {
            System.err.println("❌ Tests failed");
            System.exit(1);   // NON-ZERO → CI FAIL
        }

        System.out.println("✅ Tests passed");
        System.exit(0);       // ZERO → CI PASS
    }

    public static void main(String[] args) {
//        String scenarioPath = "helloworld/hello_world_status_ok_assertions_new.json";
//        String targetEnvPath = "config/github_host_new.properties";
//        runSingle(scenarioPath, targetEnvPath);

        String folder = "helloworld" ;
        String targetEnvPath = "config/github_host_new.properties";
        runSuite(folder, targetEnvPath);

    }
}

