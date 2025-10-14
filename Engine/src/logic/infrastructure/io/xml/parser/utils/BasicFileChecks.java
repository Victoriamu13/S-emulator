package logic.infrastructure.io.xml.parser.utils;

import java.io.InputStream;
import java.util.List;

public final class BasicFileChecks {
    private BasicFileChecks() {
    }

    public static List<String> validate(InputStream inputStream) {
        if (inputStream == null) {
            return List.of("No input stream provided.");
        }
        return List.of();
    }
}
