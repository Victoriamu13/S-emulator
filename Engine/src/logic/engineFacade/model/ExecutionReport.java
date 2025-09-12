package logic.engineFacade.model;

import java.util.Map;

public record ExecutionReport(long yValue, Map<String, Long> finalVars, long totalCycles) {
    public static ExecutionReport empty() {
        return new ExecutionReport(0L, Map.of(), 0L);
    }
}
