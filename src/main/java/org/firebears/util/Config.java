package org.firebears.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import edu.wpi.first.wpilibj.Preferences;

public final class Config {

    protected static PrintStream out = System.err;
    
    protected final static String BOOLEAN = "^(true)|(false)";
    protected final static String INTEGER = "^-?\\d+$";
    protected final static String LONG = "^-?\\d+L$";
    protected final static String FLOAT = "^-?\\d+\\.\\d*F$";
    protected final static String DOUBLE = "^-?\\d+\\.\\d*D?$";

    public static void printPreferences(PrintStream outStream) {
        final Preferences config = Preferences.getInstance();
        Collection<String> keySet = new TreeSet<String>(config.getKeys());
        for (String key : keySet) {
            out.printf("%s=%s%n", key, config.getString(key, null));
        }
    }

    public static void loadConfiguration(String... fileNames) {
        final Preferences config = Preferences.getInstance();
        for (String fileName : fileNames) {
            try {
                InputStream inStream = openStream(fileName);
                Properties properties = loadProperties(inStream);
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    if (value.trim().length()==0) {
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
                iox.printStackTrace(out);
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
        out.printf("Config: failed to open %s %n", fileName);
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
}
