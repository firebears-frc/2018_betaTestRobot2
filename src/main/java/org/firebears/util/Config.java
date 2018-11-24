package org.firebears.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;

/**
 * Utility functions for loading {@link Properties} files into WPILib's
 * {@link Preferences} objects.
 */
public final class Config {

    private static Logger logger = null;
    protected final static String BOOLEAN = "^(true)|(false)";
    protected final static String INTEGER = "^-?\\d+$";
    protected final static String LONG = "^-?\\d+L$";
    protected final static String FLOAT = "^-?\\d+\\.\\d*F$";
    protected final static String DOUBLE = "^-?\\d+\\.\\d*D?$";

    /**
     * Print out all key/value pairs in {@link Preferences}, with the keys in
     * alphabetical order.
     * 
     * @param outStream Outputstream, such as {@code System.out}.
     */
    public static void printPreferences(PrintStream outStream) {
        final Preferences config = Preferences.getInstance();
        final NetworkTable networkTable = NetworkTableInstance.getDefault().getTable("/Preferences");
        SortedSet<String> sortedKeys = new TreeSet<String>(config.getKeys());
        for (String key : sortedKeys) {
            switch (networkTable.getEntry(key).getType()) {
            case kBoolean:
                outStream.printf("%s=%b%n", key, config.getBoolean(key, true));
                break;
            case kDouble:
                outStream.printf("%s=%f%n", key, config.getDouble(key, 0.0));
                break;
            case kString:
                outStream.printf("%s=%s%n", key, config.getString(key, null));
                break;
            default:
                outStream.printf("%s=%s%n", key, config.getString(key, null));
            }
        }
    }

    /**
     * Read a sequence of property files into {@link Preferences} . If the files or
     * resources don't exist, print an error message and gracefully move to the next
     * file.
     * 
     * @param fileNames File names or resource names.
     */
    public static void loadConfiguration(String... fileNames) {
        if (logger == null) {
            logger = Logger.getLogger(Config.class.getName());
        }
        final Preferences config = Preferences.getInstance();
        for (String fileName : fileNames) {
            try {
                InputStream inStream = openStream(fileName);
                Properties properties = loadProperties(inStream);
                logger.config("Loading config from " + fileName);
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String key = entry.getKey().toString().trim();
                    String value = entry.getValue().toString();
                    if (value.length() == 0) {
                        config.remove(key);
                    } else if (value.matches(BOOLEAN)) {
                        config.putBoolean(key, Boolean.parseBoolean(value));
                    } else if (value.matches(INTEGER)) {
                        config.putInt(key, Integer.parseInt(value));
                    } else if (value.matches(LONG)) {
                        config.putLong(key, Long.parseLong(value));
                    } else if (value.matches(FLOAT)) {
                        config.putFloat(key, Float.parseFloat(value));
                    } else if (value.matches(DOUBLE)) {
                        config.putDouble(key, Double.parseDouble(value));
                    } else {
                        config.putString(key, value);
                    }
                }
            } catch (IOException iox) {
                logger.log(Level.WARNING, "Failure loading " + fileName, iox);
            }
        }
    }

    protected static InputStream openStream(String fileName) throws IOException {
        if (fileName.startsWith("/")) {
            File file = new File(fileName);
            if (file.exists() && file.canRead()) {
                return new FileInputStream(file);
            }
        } else {
            URL url = ClassLoader.getSystemResource(fileName);
            if (url != null) {
                return url.openStream();
            }
        }
        if (logger != null) {
            logger.warning("failed to open " + fileName);
        }
        return null;
    }

    protected static Properties loadProperties(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        if (inStream != null) {
            properties.load(inStream);
            inStream.close();
        }
        return properties;
    }

    /**
     * Load the java.util.logging configuration.  This should be called statically before
     * any of the logger variables have been created.
     * 
     * @param fileName File path or a resource name.
     */
    public static void loadLogConfiguration(String fileName) {
        try {
            InputStream inStream = openStream(fileName);
            LogManager logManager = LogManager.getLogManager();
            if (inStream != null) {
                logManager.readConfiguration(inStream);
                logger = Logger.getLogger(Config.class.getName());
                logger.config("Loading log config from " + fileName);
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.log(Level.WARNING, "Failed loading log configuration file: " + fileName, e);
            }
        }
    }
}
