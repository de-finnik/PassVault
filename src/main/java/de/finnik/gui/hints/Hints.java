package de.finnik.gui.hints;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * This class allows you to trigger a {@link Consumer} in your code.
 * Every trigger will be executed just one time and then be stored in a file so that there's no repetition.
 */
public class Hints {
    /**
     * The file where triggered hints will be stored
     */
    private final File file;
    /**
     * Read from {@link Hints#file}
     */
    private final Properties properties;

    /**
     * Reads already triggered hints from the given file and keeps them in {@link Hints#properties}
     *
     * @param file The file where hints are stored
     * @throws IOException Error while reading properties from input file
     */
    public Hints(File file) throws IOException {
        this.file = file;
        properties = new Properties();
        if (file.exists()) {
            properties.load(new FileReader(file));
        }
    }

    /**
     * Checks whether the given hint was already triggered and if not, triggers it.
     *
     * @param name    The name of the hint to be triggered
     * @param trigger The {@link Consumer<String>} whose {@link Consumer<String>#accept(String)} will be called when this hint hasn't be called already
     * @return Was the hint called or not?
     * @throws IOException The hint couldn't be marked as triggered inside {@link Hints#file}
     */
    public boolean triggerHint(String name, Consumer<String> trigger) throws IOException {
        boolean triggering = !Boolean.parseBoolean(properties.getProperty(name, "false"));
        if (triggering) {
            trigger.accept(name);
            properties.setProperty(name, "true");
            properties.store(new FileWriter(file), "");
        }
        return triggering;
    }
}
