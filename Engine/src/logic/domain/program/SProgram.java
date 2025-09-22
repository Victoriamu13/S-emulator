package logic.domain.program;

import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;

import java.util.List;

public interface SProgram {
String getName();
void addInstruction(SInstruction instruction);
List<SInstruction> getInstructions();
FunctionLookup getFunctionLookup();
void setFunctionLookup(FunctionLookup functions);
}
