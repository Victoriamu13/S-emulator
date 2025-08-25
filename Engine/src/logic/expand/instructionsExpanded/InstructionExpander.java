package logic.expand.instructionsExpanded;

import logic.expand.expandProgram.ExpansionContext;
import logic.instructions.SInstruction;

import java.util.List;

public interface InstructionExpander {
    List<SInstruction> expand(SInstruction inst, ExpansionContext ctx);
}
