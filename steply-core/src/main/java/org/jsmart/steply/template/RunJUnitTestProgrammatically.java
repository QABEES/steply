package org.jsmart.steply.template;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class RunJUnitTestProgrammatically {

    public static void main(String[] args) {
        // ---- Override ZeroCode annotations programmatically ----
        System.setProperty("zerocode.env", "config/github_host_new.properties");
        System.setProperty(
                "zerocode.scenario",
                "helloworld/hello_world_status_ok_assertions_new.json"
        );

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

        System.out.println("✅ All tests passed");
        System.exit(0);       // ZERO → CI PASS
    }
}

