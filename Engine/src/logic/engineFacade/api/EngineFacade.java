package logic.engineFacade.api;
import logic.domain.program.SProgram;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.engineFacade.model.InstructionDTO;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface EngineFacade {

    // ==== Load ====
    LoadOutcome loadProgram(InputStream inputStream);


    // === Program / Function selection ===
    void selectProgramOrFunction(String name);
    String getProgramName();
    SProgram getProgram();


    // === Instruction Info ===
    List<InstructionDTO> getInstructionRows(int degree);
    int getInstructionBasicCount(int degree);
    int getInstructionSyntheticCount(int degree);
    int getInstructionTotal(int degree);
    List<String> getLabelsUsed(int degree);
    List<String> getAllVariablesUsed(int degree, Integer finalIndex);
    List<String> getAllLabelsUsed(int degree, Integer finalIndex);


    // ==== Expansion ====
    void resetExpansionCache();
    List<InstructionDTO> getExpansionHistoryChain(int degree, int finalIndex);
    int getMaxExpansionDegree();


    // ==== Execution ====
    ExecutionReport runWithReport(int degree, long... inputs);
    List<String> getInputsUsed(int degree);
    long[] parseInputsCsv(String csv, int degree);
    long[] prepareInputsFields(int degree, List<String> rawValues);


    // ===== Debugging =====
    boolean isDebugActive();
    int getCurrentPc();
    boolean startDebugSession(int degree, long... inputs);
    ExecutionReport stepOver();
    ExecutionReport stepBack();
    ExecutionReport resume();
    ExecutionReport stopDebugSession();
    Set<Integer> getBreakpoints();
    void toggleBreakpoint(int idx);
    void clearAllBreakpoints();
    void setBreakpoints(Set<Integer> bps);
    ExecutionReport buildInitialReport();

    // ==== Functions ===
    List<String> getFunctionNames();

}
