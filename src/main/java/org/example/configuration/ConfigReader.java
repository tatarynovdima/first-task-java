package org.example.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final String CONFIG_FILE = "src/main/resources/config.properties";

    public static String getSourceFilePattern() {
        return getProperty("SOURCE_FILE_PATTERN");
    }

    public static int getSourceFileQueueInitialCapacity() {
        return Integer.parseInt(getProperty("SOURCE_FILE_QUEUE_INITIAL_CAPACITY"));
    }

    public static long getParseTaskTerminationTimeoutSeconds() {
        return Long.parseLong(getProperty("PARSE_TASK_TERMINATION_TIMEOUT_SECONDS"));
    }

    public static int getResultQueueInitialCapacity(){
        return Integer.parseInt(getProperty("RESULT_QUEUE_INITIAL_CAPACITY"));
    }

    private static String getProperty(String key) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            prop.load(fis);
            String value = prop.getProperty(key);
            if (value == null) {
                System.err.println("Property '" + key + "' not found in the configuration file.");
            }
            return value;
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return null;
        }
    }

}