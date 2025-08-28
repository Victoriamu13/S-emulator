package validation;

import display.EngineHolder;
import display.console.ConsoleIO;

import java.util.ArrayList;
import java.util.List;

public final class Validators {
    private Validators() {}

    public static boolean requireEngineLoaded(EngineHolder holder, ConsoleIO io) {
        if (!holder.hasEngine()) {
            io.println("Error: no valid program is loaded. Please load a program file first.");
            return false;
        }
        return true;
    }

    public static long[] parseLongsStrict(String s) {
        if (s == null || s.isBlank()) return new long[0];

        String[] parts = s.split(",");
        long[] out = new long[parts.length];
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            String token = parts[i].trim();
            int position = i + 1;

            if (token.isEmpty()) {
                // Empty token is invalid
                errors.add("Empty token at position " + position + ".");
                continue;
            }
            try {
                // Parse the token as a long. If it contains non-digits or decimal point, this will fail.
                out[i] = Long.parseLong(token);
            } catch (NumberFormatException  e) {
                errors.add("Illegal value '" + token + "' at position " + position + " — an integer is required.");
            }
        }
        if (!errors.isEmpty()) {
            // נזרוק חריגה אחת עם כל ההודעות בשורות נפרדות
            throw new IllegalArgumentException(String.join("\n", errors));
        }
        // All tokens were valid: return the filled array.
        return out;
    }

    public static int validateExpansionDegreeOrThrow(int requested, int maxDegree) {
        if (requested < 0) {
            throw new IllegalArgumentException("Expansion degree must be between 0 and "+maxDegree+". Received: " + requested);
        }
        if (requested > maxDegree) {
            throw new IllegalArgumentException(
                    "Requested expansion degree (" + requested + ") exceeds the maximum allowed (" + maxDegree + ").");
        }
        return requested;
    }


}
