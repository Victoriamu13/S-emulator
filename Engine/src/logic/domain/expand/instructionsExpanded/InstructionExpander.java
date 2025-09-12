package logic.domain.expand.instructionsExpanded;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.instructions.SInstruction;

import java.util.List;

public interface InstructionExpander {
    List<SInstruction> expand(SInstruction inst, ExpansionContext ctx);
}
