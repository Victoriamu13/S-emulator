package logic.domain.program.info;

import logic.domain.instructions.info.InstructionInfo;

import java.util.List;

public interface ProgramInfo {
    String getName();
    List<InstructionInfo> getInstructions();
    int getNumberOfInstructions();
    List<String> getInputsUsed();
    List<String> getLabelsUsed();
    List<String> getVariablesUsed();

}
