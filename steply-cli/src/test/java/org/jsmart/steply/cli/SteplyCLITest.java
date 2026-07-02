package org.jsmart.steply.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for SteplyCLI argument parsing using Picocli.
 * <p>
 * Tests invoke SteplyCLI.run(String[] args) returning an exit code,
 * so tests can call it directly without intercepting System.exit().
 */
public class SteplyCLITest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void helpOption_shouldPrintUsageAndExit0() {

        int status = SteplyCLI.run(new String[]{"-h"});

        assertEquals(0, status);

        String out = outContent.toString();
        assertTrue("Help output should contain usage information",
                out.toLowerCase().contains("usage: steply"));
    }

    @Test
    public void versionOption_shouldPrintVersionAndExit0() {

        int status = SteplyCLI.run(new String[]{"-V"}); // Picocli mixin StandardHelpOptions uses -V for version

        assertEquals(0, status);

        String out = outContent.toString();
        assertTrue("Version output should mention Steply Test Execution",
                out.contains("Steply Test Execution Version"));
    }

    @Test
    public void noArgs_shouldPrintUsage_andExit1() {

        int status = SteplyCLI.run(new String[]{});

        // Since we explicitly override call() in SteplyCLI to print usage and return 1
        assertEquals(1, status);

        String out = outContent.toString();
        assertTrue("Output should contain usage", out.toLowerCase().contains("usage: steply"));
    }

    @Test
    public void runCommand_noArgs_shouldPrintError_andExit2() {

        int status = SteplyCLI.run(new String[]{"run"});

        assertEquals(2, status);

        String err = errContent.toString();
        assertTrue(err.contains("Either a scenario file OR --folder/--suite must be provided."));
        assertTrue(err.toLowerCase().contains("usage: steply run"));
    }

    @Test
    public void runCommand_missingFile_shouldPrintError_andExit2() {

        int status = SteplyCLI.run(new String[]{"run", "does-not-exist.json"});

        assertEquals(2, status);

        String err = errContent.toString();
        assertTrue(err.contains("Error: Scenario file does not exist:"));
    }

    @Test
    public void runCommand_missingTarget_shouldPrintWarning_butProceedExecution() {

        int status = SteplyCLI.run(new String[]{"run", "pom.xml"});

        // exit code 1 confirms execution proceeded past parsing and ran tests which naturally failed on pom.xml
        assertEquals(1, status);

        String err = errContent.toString();
        assertTrue(err.contains("Running in default mode."));
        assertTrue(err.contains("Tests failed"));
    }

    @Test
    public void runCommand_withTargetEnv_shouldProceedExecution() {

        int status = SteplyCLI.run(new String[]{"run", "pom.xml", "-t", "env.properties"});

        assertEquals(2, status);

        String err = errContent.toString();
        assertTrue(err.contains("Execution failed:"));
    }

    @Test
    public void invalidCommand_shouldPrintError_andExit2() {

        int status = SteplyCLI.run(new String[]{"unknownCommand"});

        assertEquals(2, status);

        String err = errContent.toString();
        assertTrue(err.contains("Unmatched argument at index 0: 'unknownCommand'"));
    }

    @Test
    public void logsCommand_help_shouldPrintUsageAndExit0() {

        int status = SteplyCLI.run(new String[]{"logs", "--help"});

        assertEquals(0, status);

        String out = outContent.toString();
        assertTrue(out.toLowerCase().contains("usage: steply logs"));
    }
}
