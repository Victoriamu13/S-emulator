package logic.domain.execution.executer;

import logic.engineFacade.model.ExecutionReport;

import java.util.Set;

public interface ProgramExecuter {
    ExecutionReport runWithReport(Set<Integer> breakpoints,long... input);
}
