package org.borman.util;

import org.joda.time.DateTime;

/**
 * A class used to print out the current timestamp.
 * Intended to be used by html views that are leveraging Thymeleaf ({@see http://www.thymeleaf.org/})
 * to put a timestamp on .js and .css file import statements to prevent caching.
 */
public class TimestampPrinter {
    /**
     * Returns A {@link java.lang.String} representation of the current timestamp.
     */
    public String print() {
        return Long.toString(DateTime.now().getMillis());
    }
}
