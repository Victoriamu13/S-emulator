package logic.execution.executer;

import logic.engineFacade.report.ExecutionReport;

public interface ProgramExecuter {
    long run(long... input);
    ExecutionReport runWithReport(long... input);
}
