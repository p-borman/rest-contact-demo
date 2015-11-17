package org.borman.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimestampPrinterTest {
    @Test
    public void testShouldGetTimeStamp() {
        DateTime now = DateTime.now();

        String ts = new TimestampPrinter().print();

        DateTime dateTime = new DateTime(Long.parseLong(ts), now.getZone());
        assertThat(dateTime.toDate()).isCloseTo(now.toDate(), 500);
    }
}