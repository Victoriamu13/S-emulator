package logic.io.xml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class BasicFileChecks {
private BasicFileChecks() {}

    static List<String> validate(Path xmlPath){
        List<String> errors = new ArrayList<>();
        if (xmlPath == null) {
            errors.add("No file path was provided.");
            return errors;
        }
        if (!Files.exists(xmlPath)) {
            errors.add("File not found: " + xmlPath);
            return errors;
        }
        String name = xmlPath.getFileName().toString().toLowerCase().trim();
        if (!name.endsWith(".xml")) {
            errors.add("File must have .xml extension.");
        }
        return errors;
    }
}
