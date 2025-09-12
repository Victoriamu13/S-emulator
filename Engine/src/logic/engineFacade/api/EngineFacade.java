package logic.engineFacade.api;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.program.info.ProgramInfo;
import logic.engineFacade.model.InstructionDTO;

import java.nio.file.Path;
import java.util.List;

public interface EngineFacade {

    //---Load program---
    LoadOutcome loadProgram(Path xmlPath);
    boolean hasProgram();
    String getLoadedXmlPath();

    //---Program info---
    List<String> getInputsUsed(int degree);
    List<String> getLabelsUsed(int degree);
    String getProgramName();
    int getMaxExpansionDegree();

    //---instructions---
    List<InstructionDTO> getInstructionRows(int degree);
    List<InstructionDTO> getExpansionHistoryChain(int degree, int finalIndex);
    int getInstructionBasicCount(int degree);
    int getInstructionSyntheticCount(int degree);
    int getInstructionTotal(int degree);

    //---Execute program---
    ExecutionReport runWithReport(int degree, long... inputs);

}
