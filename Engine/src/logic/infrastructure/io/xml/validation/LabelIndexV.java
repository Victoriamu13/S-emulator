package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public class LabelIndexV {
    private final Map<String, Integer> labelCountsUpper; // label -> occurrences
    private final List<String> errors;


    private LabelIndexV(Map<String, Integer> countsUpper, List<String> errors) {
        this.labelCountsUpper = countsUpper;
        this.errors = errors;
    }

    static LabelIndexV build(List<RawInstructions> raw) {
        Map<String, Integer> labelCounts = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (RawInstructions r : raw) {
            String label = makeValidString(r.labelText());
            if (label.isEmpty()) continue;

            // EXIT cannot be a line-label
            if (equalIgnoreCase(label, "EXIT")) {
                errors.add(ValidationUtils.msg(r, "'EXIT' cannot be used as a line label."));
                continue;
            }

            // must be L1..L99
            if (!label.matches("(?i)L([1-9]|[1-9][0-9])")) {
                errors.add(ValidationUtils.msg(r,
                        "Invalid line label '" + label + "' (must be L1..L99)."));
                continue;
            }

            String key = label.toUpperCase(Locale.ROOT);
            labelCounts.put(key, labelCounts.getOrDefault(key, 0) + 1);
        }

        // duplicates
        for (Map.Entry<String, Integer> e : labelCounts.entrySet()) {
            if (e.getValue() > 1) {
                errors.add("Label '" + e.getKey() + "' defined " + e.getValue() + " times.");
            }
        }

        return new LabelIndexV(labelCounts, errors);
    }

    Set<String> definedLabelsUpper() {
        return labelCountsUpper.keySet();
    }

    List<String> errors() {
        return errors;
    }

}
