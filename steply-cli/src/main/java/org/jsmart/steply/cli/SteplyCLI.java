package org.jsmart.steply.cli;

import org.jsmart.steply.cli.commands.LogsCommand;
import org.jsmart.steply.cli.commands.RunCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

@Command(
        name = "steply",
        mixinStandardHelpOptions = true,
        versionProvider = SteplyCLI.PropertiesVersionProvider.class,
        description = "Steply CLI",
        subcommands = {
                RunCommand.class,
                LogsCommand.class
        }
)
public class SteplyCLI implements Callable<Integer> {

    public static void main(String[] args) {
        System.exit(run(args));
    }

    /**
     * Executes CLI logic and returns an exit code.
     * This method exists so tests can invoke CLI logic
     * without triggering System.exit().
     */
    public static int run(String[] args) {
        return new CommandLine(new SteplyCLI()).execute(args);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 1;
    }

    static class PropertiesVersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() {
            String home = System.getProperty("steply.home", ".");
            File versionFile = new File(home, "VERSION.txt");

            if (versionFile.exists()) {
                try (FileInputStream fis = new FileInputStream(versionFile)) {

                    Properties props = new Properties();
                    props.load(fis);

                    String version = props.getProperty("steply.version");
                    if (version != null) {
                        return new String[]{"Steply Test Execution Version " + version};
                    }

                } catch (IOException ignored) {
                    System.out.println("Could not read version info. You can safely ignore this.");
                }
            }
            return new String[]{"Steply Test Execution Version unknown"};
        }
    }
}