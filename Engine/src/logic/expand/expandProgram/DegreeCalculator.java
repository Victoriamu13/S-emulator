package logic.expand.expandProgram;

import logic.instructions.SInstruction;
import logic.expand.expandProgram.ProgramExpander;
import logic.program.SProgram;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DegreeCalculator {
    private final ProgramExpander expander;

    public DegreeCalculator(ProgramExpander expander) {
        this.expander = expander;
    }

    private final Map<SInstruction,Integer> memo = new IdentityHashMap<>();

    public int degreeOfInstruction(SInstruction ins) {
        List<SInstruction> current = List.of(ins);
        int degree = 0;

        // Expand instructions until all of them are basic
        while (true) {
            boolean allBasic = true;
            List<SInstruction> next = new ArrayList<>();

            for (SInstruction inst : current) {
                List<SInstruction> expanded = expander.expandOne(inst);

                if (expanded.size() == 1 && expanded.getFirst() == inst) { //if basic
                    next.add(inst);

                } else {// synthetic - keep expanding
                    allBasic = false;
                    next.addAll(expanded);
                }
            }
            if (allBasic) {break;}
            degree++;      // finished one round of expanding
            current = next; // continue to next round with collected instructions
        }

        return degree;
    }

    public int maxProgramDegree(SProgram program) {
        int max = 0;
        for (SInstruction ins : program.getInstructions()) {
            max = Math.max(max, degreeOfInstruction(ins));
        }
        return max;
    }
}
