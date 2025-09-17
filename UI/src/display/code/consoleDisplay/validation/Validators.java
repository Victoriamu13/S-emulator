package consoleDisplay.validation;


import java.util.ArrayList;
import java.util.List;

public final class Validators {
    private Validators() {}

    public static boolean requireEngineLoaded(engineHolder.EngineHolder holder, consoleDisplay.console.ConsoleIO io) {
        if (!holder.hasEngine()) {
            io.println("Error: no valid program is loaded. Please load a program file first.");
            return false;
        }
        return true;
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
