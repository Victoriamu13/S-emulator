package logic.engineFacade.model;

import java.util.Map;
import java.util.Set;

public record ExecutionReport(long yValue,                     // final Y value
                              Set<String> changedVars,        // variables that changed during execution
                              Map<String, Long> finalVars,   // final state of all variables
                              long totalCycles) {            // total cycles consumed during execution

   // Empty execution report
    public static ExecutionReport empty() {
        return new ExecutionReport(0L, Set.of(), Map.of(), 0L);
    }
}
