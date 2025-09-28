package logic.engineFacade.model;

import java.util.Map;
import java.util.Set;

public record ExecutionReport(long yValue,Set<String> changedVars,Map<String, Long> finalVars, long totalCycles) {
    public static ExecutionReport empty() {
        return new ExecutionReport(0L, Set.of(), Map.of(), 0L);
    }
}
