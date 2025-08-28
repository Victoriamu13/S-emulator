package logic.engineFacade.facade;
import logic.engineFacade.report.ExecutionReport;
import logic.program.info.ProgramInfo;

import java.nio.file.Path;

public interface EngineFacade {

    //---Load program---
    boolean loadProgram(Path xmlPath);
    boolean hasProgram();

    //---Program info---
    ProgramInfo getProgramInfo(int degree);
    int getMaxExpansionDegree();

    //---Execute program---
    ExecutionReport runWithReport(int degree, long... inputs);
    default long run(int degree, long... inputs) {     //run without report
        return runWithReport(degree, inputs).yValue();
    }
}
