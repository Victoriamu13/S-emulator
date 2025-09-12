package logic.domain.program;

import logic.domain.instructions.SInstruction;

import java.util.List;

public interface SProgram {
String getName();
void addInstruction(SInstruction instruction);
List<SInstruction> getInstructions();
}
