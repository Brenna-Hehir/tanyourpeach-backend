package com.tanyourpeach.backend.util;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/** Minimal MySQL DATE_FORMAT compat for tests (H2). */
public class H2MysqlCompat {

    /** H2 ALIAS target: matches MySQL DATE_FORMAT(ts, fmt). */
    public static String dateFormat(Timestamp ts, String mysqlFmt) {
        if (ts == null || mysqlFmt == null) return null;

        // Cheap mapping for common tokens used in your query:
        // %Y-%m -> yyyy-MM (you can expand if needed)
        String javaFmt = mysqlFmt
                .replace("%Y", "yyyy")
                .replace("%m", "MM")
                .replace("%d", "dd")
                .replace("%H", "HH")
                .replace("%i", "mm") // minutes
                .replace("%s", "ss"); // seconds

        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern(javaFmt));
    }
}