package org.jsmart.steply.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple properties loader for target environment properties.
 */
public class SteplyConfigLoader {

    public Map<String, String> loadEnvironmentProperties(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(filePath)) {
            props.load(in);
        }
        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return map;
    }

    public void updateLoggingLevel(String level) {
        // Placeholder: We can modify logback programmatically here in a future step.
        // For now we set environment variable for logback to pick up, if needed.
        if (level != null) {
            System.setProperty("LOG_LEVEL", level);
        }
    }
}