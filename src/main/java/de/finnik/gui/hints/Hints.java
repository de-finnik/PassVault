package de.finnik.gui.hints;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

public class Hints {
    private final File file;
    private final Properties properties;

    public Hints(File file) throws IOException {
        this.file = file;
        properties = new Properties();
        if (file.exists()) {
            properties.load(new FileReader(file));
        }
    }

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
