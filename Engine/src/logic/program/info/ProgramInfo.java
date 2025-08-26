package logic.program.info;

import logic.instructions.info.InstructionInfo;

import java.util.List;

public interface ProgramInfo {
    String getName();
    List<InstructionInfo> getInstructions();
    int getNumberOfInstructions();
    List<String> getInputsUsed();
    List<String> getLabelsUsed();

}
