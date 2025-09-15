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
    String getProgramName();

    //---instructions---
    List<InstructionDTO> getInstructionRows(int degree);
    List<InstructionDTO> getExpansionHistoryChain(int degree, int finalIndex);
    int getInstructionBasicCount(int degree);
    int getInstructionSyntheticCount(int degree);
    int getInstructionTotal(int degree);
    List<String> getInputsUsed(int degree);
    List<String> getVariablesUsed(int degree);
    List<String> getLabelsUsed(int degree);
    List<String> getAllLabelsUsed(int degree, Integer finalIndex);
    List<String> getAllVariablesUsed(int degree, Integer finalIndex);


    //---Expansion---
    int getMaxExpansionDegree();

    //---Execute program---
    ExecutionReport runWithReport(int degree, long... inputs);

}
