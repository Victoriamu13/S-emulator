package logic.engineFacade.model.debug;
import logic.domain.variable.SVars;

import java.util.Map;

public record DebugState(int pc, long totalCycles, Map<SVars,Long> snapshot) { }
