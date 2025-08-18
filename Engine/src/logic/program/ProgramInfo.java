package logic.program;

import logic.instructions.InstructionInfo;
import logic.instructions.SInstruction;

import java.util.List;

public interface ProgramInfo {
    String getName();
    List<InstructionInfo> getInstructions();
    int getNumberOfInstructions();
    List<String> getInputsUsed();
    List<String> getLabelsUsed();

}
