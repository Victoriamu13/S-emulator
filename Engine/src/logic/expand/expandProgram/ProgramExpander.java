package logic.expand.expandProgram;

import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.SInstruction;

import java.util.ArrayList;
import java.util.List;

public class ProgramExpander {
    private final ExpansionContext ctx;

    public ProgramExpander(ExpansionContext ctx) { this.ctx = ctx; }

    public List<SInstruction> expandOne(SInstruction inst) {
        InstructionExpander ex = ExpanderFactory.forInstruction(inst);
        if (ex == null) return List.of(inst);    // basic instruction
        return ex.expand(inst, ctx);    // expand synthetic instruction
    }

    public List<SInstruction> expandToDegree(List<SInstruction> prog, int degree) {
        List<SInstruction> cur = prog;

        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<SInstruction> next = new ArrayList<>();

            for (SInstruction q : cur) {
                InstructionExpander ex = ExpanderFactory.forInstruction(q);
                if (ex == null) { next.add(q); continue; }  // basic instruction- just add to list
                allBasic = false;
                next.addAll(ex.expand(q, ctx)); //expand synthetic instruction and add to list
            }

            cur = next;
            if (allBasic) break; // all instructions are basic - end expander
        }
        return cur;
    }

}
