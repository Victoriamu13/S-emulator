package logic.engineFacade.model;

import java.io.Serializable;
import java.util.Map;

public record RunRecord(int runNo, int degree, long[] inputs, long yValue, long cycles, Map<String, Long> finalVars ) implements Serializable {
    private static final long serialVersionUID = 1L;
}
