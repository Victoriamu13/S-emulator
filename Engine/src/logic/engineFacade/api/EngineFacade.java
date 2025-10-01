package logic.engineFacade.api;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.engineFacade.model.InstructionDTO;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface EngineFacade {

    //---Load program---
    LoadOutcome loadProgram(Path xmlPath);
    boolean hasProgram();
    String getLoadedXmlPath();

    //---Program info---
    void selectProgramOrFunction(String name);
    String getProgramName();

    //---instructions---
    List<InstructionDTO> getInstructionRows(int degree);
    int getInstructionBasicCount(int degree);
    int getInstructionSyntheticCount(int degree);
    int getInstructionTotal(int degree);
    List<String> getLabelsUsed(int degree);
    List<String> getAllLabelsUsed(int degree, Integer finalIndex);
    List<String> getAllVariablesUsed(int degree, Integer finalIndex);


    //---Expansion---
    int getMaxExpansionDegree();
    List<InstructionDTO> getExpansionHistoryChain(int degree, int finalIndex);
    void resetExpansionCache();

    //---Execute program---
    List<String> getInputsUsed(int degree);
    public long[] parseInputsCsv(String csv, int degree);
    long[] prepareInputsFields(int degree, List<String> rawValues);
    ExecutionReport runWithReport(int degree,long... inputs);

    //---Debug---
    boolean startDebugSession(int degree, long[] inputs);
    ExecutionReport stepOver();
    ExecutionReport stepBack();
    ExecutionReport resume();
    ExecutionReport stopDebugSession();
    boolean isDebugActive();
    int getCurrentPc();
    ExecutionReport buildInitialReport();
    void clearAllBreakpoints();
    void setBreakpoints(Set<Integer> breakpoints);
    Set<Integer> getBreakpoints();
    void toggleBreakpoint(int idx);

    //---Functions---
    List<String> getFunctionNames();
    List<InstructionDTO> getFunctionInstructionRows(String functionName);
}
