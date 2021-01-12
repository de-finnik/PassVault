package de.finnik.passvault;

import org.slf4j.Logger;

import java.io.OutputStream;

/**
 * Redirects all written content to a given {@link Logger} object
 */
public class LogErrorStream extends OutputStream {
    Logger logger;
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