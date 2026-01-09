package org.jsmart.steply.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class SteplyCommandRunnerTest {

    private File targetEnvFile;

    @Before
    public void setUp() throws IOException {
        // create a temporary target environment properties file
        targetEnvFile = Files.createTempFile("target-env-", ".properties").toFile();
        targetEnvFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (targetEnvFile != null && targetEnvFile.exists()) {
            targetEnvFile.delete();
        }
    }

    private void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File c : files) {
                    deleteRecursively(c);
                }
            }
        }
        f.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowWhenScenarioFileMissing() {
        // scenario provided but does not exist, target exists
        File nonExistent = new File("/does/not/exist/scenario.json");
        SteplyCommandRunner runner = new SteplyCommandRunner(nonExistent.getAbsolutePath(), null, targetEnvFile.getAbsolutePath(), null, "INFO");
        runner.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowWhenSuiteFolderMissing() {
        File nonExistentFolder = new File("/does/not/exist/folder");
        SteplyCommandRunner runner = new SteplyCommandRunner(null, nonExistentFolder.getAbsolutePath(), targetEnvFile.getAbsolutePath(),    null, "INFO");
        runner.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowWhenTargetEnvMissing() {
        File missingTarget = new File("/does/not/exist/target.properties");
        // pass null scenario/suite so that the validation reaches the target check
        SteplyCommandRunner runner = new SteplyCommandRunner(null, null, missingTarget.getAbsolutePath(),    null, "INFO");
        runner.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void runSingleScenario_shouldThrowWhenNoScenarioProvided() {
        SteplyCommandRunner runner = new SteplyCommandRunner(null, null, targetEnvFile.getAbsolutePath(),    null, "INFO");
        runner.runSingleScenario();
    }

    @Test(expected = IllegalStateException.class)
    public void runSuite_shouldThrowWhenNoSuiteProvided() {
        SteplyCommandRunner runner = new SteplyCommandRunner(null, null, targetEnvFile.getAbsolutePath(),    null, "INFO");
        runner.runSuite();
    }

}