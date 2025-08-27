package logic.engineFacade.facade;
import logic.engineFacade.report.ExecutionReport;
import logic.program.info.ProgramInfo;

public interface EngineFacade {
    ProgramInfo getProgramInfo(int degree);
    int getMaxExpansionDegree();
    ExecutionReport runWithReport(int degree, long... inputs);

    //run without report
    default long run(int degree, long... inputs) {
        return runWithReport(degree, inputs).yValue();
    }
}
