package logic.domain.expand.expandProgram;

import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;

import java.util.ArrayList;
import java.util.List;

public class ProgramExpander {
    private final ExpansionContext ctx;  // holds counters + lookup

    public ProgramExpander(ExpansionContext ctx) { this.ctx = ctx; }

    // Expand one instruction (basic stays as is, synthetic expands)
    public List<SInstruction> expandOne(SInstruction inst) {
        InstructionExpander ex = ExpanderFactory.forInstruction(inst,ctx);
        return (ex == null) ? java.util.List.of(inst) : ex.expand(inst, ctx);
    }

    // Expand program to requested degree
    public List<SInstruction> expandToDegree(List<SInstruction> prog, int degree) {
        List<SInstruction> cur = prog;

        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<SInstruction> next = new ArrayList<>();

            for (SInstruction q : cur) {
                InstructionExpander ex = ExpanderFactory.forInstruction(q, ctx);
                if (ex == null) {
                    next.add(q);     // basic → keep
                } else {
                    allBasic = false;   // found synthetic
                    next.addAll(ex.expand(q, ctx));
                }
            }

            cur = next;
            if (allBasic) break;  // finished expansion
        }
        return cur;
    }

}
