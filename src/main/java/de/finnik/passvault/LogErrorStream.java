package de.finnik.passvault;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.OutputStream;

public class LogErrorStream extends OutputStream {
    Logger logger;
    Level level;
    StringBuilder stringBuilder;

    public LogErrorStream(Logger logger) {
        this.logger = logger;
        stringBuilder = new StringBuilder();
    }

    @Override
    public final void write(int i) {
        char c = (char) i;
        if (c == '\r' || c == '\n') {
            if (stringBuilder.length() > 0) {
                logger.error(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        } else
            stringBuilder.append(c);
    }
}