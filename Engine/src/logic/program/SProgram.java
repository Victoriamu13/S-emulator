package logic.program;

import logic.Instructions.SInstruction;
import java.util.List;

public interface SProgram {
String getName();
List<SInstruction> getInstructions();
}
