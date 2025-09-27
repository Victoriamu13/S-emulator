package logic.domain.expand.expandProgram;

import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;

import java.util.ArrayList;
import java.util.List;

public class ProgramExpander {
    private final ExpansionContext ctx;

    public ProgramExpander(ExpansionContext ctx) { this.ctx = ctx; }

    public List<SInstruction> expandOne(SInstruction inst) {
        InstructionExpander ex = ExpanderFactory.forInstruction(inst,ctx);
        return (ex == null) ? java.util.List.of(inst) : ex.expand(inst, ctx);
    }

    public List<SInstruction> expandToDegree(List<SInstruction> prog, int degree) {
        List<SInstruction> cur = prog;

        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<SInstruction> next = new ArrayList<>();

            for (SInstruction q : cur) {
                InstructionExpander ex = ExpanderFactory.forInstruction(q, ctx);
                if (ex == null) {
                    next.add(q);    // basic instruction- just add to list
                } else {
                    allBasic = false;
                    List<SInstruction> expanded = ex.expand(q, ctx);
                    next.addAll(expanded);  //expand synthetic instruction and add to list
                }
            }

            cur = next;
            if (allBasic) break; // all instructions are basic - end expander
        }
        return cur;
    }

}
