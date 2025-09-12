package logic.domain.execution.executer;

import logic.engineFacade.model.ExecutionReport;

public interface ProgramExecuter {
    long run(long... input);
    ExecutionReport runWithReport(long... input);
}
