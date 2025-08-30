package logic.engineFacade.facade;
import logic.engineFacade.report.ExecutionReport;
import logic.io.xml.load.LoadResult;
import logic.program.info.ProgramInfo;

import java.nio.file.Path;

public interface EngineFacade {

    //---Load program---
    LoadResult loadProgram(Path xmlPath);
    boolean hasProgram();
    String getLoadedXmlPath();

    //---Program info---
    ProgramInfo getProgramInfo(int degree);
    int getMaxExpansionDegree();

    //---Execute program---
    ExecutionReport runWithReport(int degree, long... inputs);

}
