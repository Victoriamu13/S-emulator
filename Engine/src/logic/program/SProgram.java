package logic.program;

import logic.instructions.SInstruction;

import java.util.List;

public interface SProgram {
String getName();
void addInstruction(SInstruction instruction);
List<SInstruction> getInstructions();
}
