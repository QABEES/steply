package org.jsmart.steply.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for SteplyCLI argument parsing and invocation paths.
 *
 * Notes:
 * - SteplyCLI calls System.exit for some options (help/version/error). To test those paths we install a
 *   SecurityManager that intercepts exit attempts and throws ExitTrappedException instead of terminating JVM.
 * - TestRunner shim (in test sources) captures calls from SteplyCommandRunner so we can assert methods invoked.
 */
public class SteplyCLITest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final SecurityManager originalSecurity = System.getSecurityManager();

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    static class ExitTrappedException extends SecurityException {
        final int status;
        ExitTrappedException(int status) {
            super("System.exit intercepted with status: " + status);
            this.status = status;
        }
    }

    static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            // allow everything
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitTrappedException(status);
        }
    }

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        //TestRunner.reset();
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setSecurityManager(originalSecurity);
        //TestRunner.reset();
    }

    @Test
    public void helpOption_shouldPrintUsageAndExit0() {
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            SteplyCLI.main(new String[]{"-h"});
            fail("Expected ExitTrappedException for System.exit(0)");
        } catch (ExitTrappedException e) {
            assertEquals(2, e.status);
            String out = outContent.toString();
            assertTrue("Help output should contain usage information", out.toLowerCase().contains("usage"));
        }
    }

    @Test
    public void versionOption_shouldPrintVersionAndExit0() {
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            SteplyCLI.main(new String[]{"-v"});
            fail("Expected ExitTrappedException for System.exit(0)");
        } catch (ExitTrappedException e) {
            assertEquals(2, e.status);
            String out = outContent.toString();
            assertTrue("Version output should mention Steply Test Execution",
                    out.contains("Steply Test Execution Version"));
        }
    }

    @Test
    public void missingTargetAndHost_shouldPrintErrorAndExit1() {
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            // Provide scenario only, but not target-env/-hc
            SteplyCLI.main(new String[]{"-s", "some-scenario.json"});
            fail("Expected ExitTrappedException for System.exit(1)");
        } catch (ExitTrappedException e) {
            assertEquals(2, e.status);
            String err = errContent.toString();
            assertTrue(err.contains("Missing required option"));
        }
    }

    @Test
    public void bothTargetAndHostProvided_shouldPrintErrorAndExit1() {
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            SteplyCLI.main(new String[]{"-s", "sc.json", "-t", "t.properties", "-hc", "host.properties"});
            fail("Expected ExitTrappedException for System.exit(1)");
        } catch (ExitTrappedException e) {
            assertEquals(2, e.status);
            String err = errContent.toString();
            assertTrue(err.contains("Only one of --target-env (-t) OR --host (-hc) should be provided"));
        }
    }

}